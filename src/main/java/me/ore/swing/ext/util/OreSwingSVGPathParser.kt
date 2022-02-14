package me.ore.swing.ext.util

import me.ore.swing.ext.OreSwingExt
import me.ore.swing.ext.OreSwingExtJava
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Object to read [Path2D.Double] from SVG path definition (the "d" attribute of the `<path>` element).
 *
 * See [SVG path definition at https://developer.mozilla.org/](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d).
 *
 * @see parse
 */
object OreSwingSVGPathParser {
    /**
     * Class of objects that contain data about the current state when reading the SVG path definition
     *
     * @param index Index of the current character from which one of the next read or check commands will start
     * @param path SVG path definition
     * @param result Shape which represents an arbitrary geometric path; this figure will be returned by the [parse] method
     */
    private class Data(
        var index: Int,
        val path: String,
        val result: Path2D.Double
    ) {
        /**
         * The last control point used when drawing the cubic Bézier curve;
         * used in commands for drawing smooth cubic Bézier curve ("continuation" of cubic Bézier curve) - "S" and "s"
         */
        var prevCubicBezierPoint: Point2D.Double? = null

        /**
         * The last control point used when drawing the quadratic Bézier curve;
         * used with commands for drawing smooth quadratic Bézier curve ("continuation" quadratic Bézier curve) - "T" and "t"
         */
        var prevQuadraticBezierPoint: Point2D.Double? = null

        /**
         * Current character from which one of the next read or check commands will start
         *
         * If [index] is greater than or equal to the length of [path], the current character will be `null`; otherwise - `path[index]`
         */
        val char: Char?
            get() {
                return if ((this.index >= 0) && (this.index < this.path.length)) {
                    this.path[this.index]
                } else {
                    null
                }
            }

        /**
         * "Current" (or "last") point in [result]
         *
         * If `result`.[getCurrentPoint()][Path2D.Double.getCurrentPoint] returns non-null value, then `point` is equal to that value;
         * otherwise - `point` is equal to [ZERO_POINT]
         */
        val point: Point2D
            get() = this.result.currentPoint ?: ZERO_POINT

        /**
         * Increases [index] by 1
         */
        fun incIndex() {
            this.index++
        }

        /**
         * Deletes previous control points for Bézier curves
         *
         * @param cubicBezier If `true`, sets [prevCubicBezierPoint] to `null`; default is `true`
         * @param quadraticBezier If `true`, sets [prevQuadraticBezierPoint] to `null`; default is `true`
         */
        fun clearPrev(cubicBezier: Boolean = true, quadraticBezier: Boolean = true) {
            if (cubicBezier)
                this.prevCubicBezierPoint = null

            if (quadraticBezier)
                this.prevQuadraticBezierPoint = null
        }
    }


    /**
     * Part type in SVG path definition:
     *
     * * [COMMAND] - part is command
     * * [NUMBER] - part is number
     */
    private enum class Token {
        /**
         * Part is command
         */
        COMMAND,

        /**
         * Part is number
         */
        NUMBER
    }


    /**
     * Point with coordinates 0 × 0
     */
    private val ZERO_POINT = Point2D.Double(0.0, 0.0)


    // region Cache
    /**
     * Using the cache
     *
     * If `useCache` is `true` then read [Path2D.Double] will be cached.
     * In this case, calling [parse] again with the SVG path definition already passed in will return the cached [Path2D.Double] object
     * instead of rereading [Path2D.Double] from the SVG path definition
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var useCache: Boolean = false

    /**
     * Cache
     *
     * Map "SVG path definition -> saved { [Path2D.Double] with optional parse exception }"
     */
    private val CACHE = HashMap<String, Pair<Path2D.Double, Exception?>>()

    /**
     * Store `path` and `exception` in [CACHE] with `pathString` as key
     *
     * @param pathString SVG path definition, which is used as a key in [CACHE]
     * @param path Stored path
     * @param exception Stored error parsing `pathString`
     */
    private fun cache(pathString: String, path: Path2D.Double, exception: Exception? = null) {
        synchronized(CACHE) { CACHE[pathString] = Path2D.Double(path) to exception }
    }

    /**
     * Getting stored in [CACHE] path and reading error
     *
     * @param pathString SVG path definition to get the path and error for
     * @param leaveIncompleteOnError If equal to `true` or there was no error while reading `pathString`,
     * then returns a copy of the found path with a read error (if there is an error);
     * otherwise, returns the original "path + read error" pair
     *
     * @return Pair "path + read error"; if nothing is found in the cache, returns `null`
     */
    private fun getFromCache(pathString: String, leaveIncompleteOnError: Boolean): Pair<Path2D.Double, Exception?>? {
        return synchronized(CACHE) { CACHE[pathString] }
            ?.let { pair ->
                val (path, exception) = pair
                if (leaveIncompleteOnError || (exception == null)) {
                    Path2D.Double(path) to exception
                } else {
                    pair
                }
            }
    }
    // endregion


    // region Getting next parts
    /**
     * Getting the type of the next part of the path from `data`
     *
     * @param data Data about the current state when reading the SVG path definition
     *
     * @return Type of the next part; if reading the path is already complete, returns `null`
     */
    private fun nextToken(data: Data): Token? {
        while (true) {
            val char = data.char
                ?: return null

            when {
                (char.isLetter()) -> return Token.COMMAND
                (char.isDigit() || (char == '-') || (char == '.')) -> return Token.NUMBER
            }

            data.incIndex()
        }
    }

    /**
     * Getting the next command from `data`
     *
     * @param data Data about the current state when reading the SVG path definition
     *
     * @return Next command, for example "M" or "s"; if reading the path is already complete, returns `null`
     *
     * @throws IllegalArgumentException If the next part of the path is a number
     */
    private fun nextCommand(data: Data): Char? {
        val indexOfParsing = data.index
        when (nextToken(data)) {
            Token.COMMAND -> {}
            Token.NUMBER -> throw IllegalArgumentException("Expected command, but found number at or after index $indexOfParsing (SVG path \"${data.path}\")")
            null -> return null
        }

        while (true) {
            val char = data.char
                ?: return null

            if (char.isLetter()) {
                data.index++
                return char
            }

            data.incIndex()
        }
    }

    /**
     * Getting the next number from `data`
     *
     * @param data Data about the current state when reading the SVG path definition
     *
     * @return Next number; if reading the path is already complete, returns `null`
     *
     * @throws IllegalArgumentException If the next part of the path is a command; if part of the path cannot be read as a number
     */
    private fun nextNumber(data: Data): Double? {
        val indexOfParsing = data.index
        when (nextToken(data)) {
           null -> return null
           Token.NUMBER -> {}
           Token.COMMAND -> throw IllegalArgumentException("Expecting number but found command starting at index $indexOfParsing (SVG path \"${data.path}\")")
        }

        var minusFound = false
        var dotFound = false
        var startIndex: Int? = null
        var endIndex: Int? = null

        while (true) {
            val char = data.char
            if (char == null) {
                if (startIndex != null) {
                    endIndex = data.index
                }
                break
            }

            when {
                char.isDigit() -> {
                    if (startIndex == null) {
                        startIndex = data.index
                    }
                }
                (char == '-') -> {
                    if (minusFound || dotFound) {
                        endIndex = data.index
                        break
                    }

                    if (startIndex == null) {
                        startIndex = data.index
                        minusFound = true
                    } else {
                        endIndex = data.index
                        break
                    }
                }
                (char == '.') -> {
                    if (dotFound) {
                        endIndex = data.index
                        break
                    }

                    dotFound = true

                    if (startIndex == null) {
                        startIndex = data.index
                    }
                }
                else -> {
                    endIndex = data.index
                    break
                }
            }

            data.incIndex()
        }

        return if ((startIndex != null) && (endIndex != null)) {
            val substring = data.path.substring(startIndex, endIndex)
            try {
                substring.toDouble()
            } catch (e: Exception) {
                throw IllegalArgumentException("Cannot parse number starting at index $indexOfParsing (SVG path \"${data.path}\")")
            }
        } else {
            null
        }
    }
    // endregion


    // region Requiring parts
    /**
     * Request next number from `data`
     *
     * @param T Type of result returned by `block`
     * @param data Data about the current state when reading the SVG path definition
     * @param block Block of code to which the read number is passed
     *
     * @return Result of calling `block`
     *
     * @throws IllegalArgumentException If the next part of the path is a command;
     * if part of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private inline fun <T> requireNumber(data: Data, block: (number: Double) -> T): T {
        val indexOfParsing = data.index
        val number = nextNumber(data)
            ?: throw IllegalArgumentException("Expected number, but number not found starting at index $indexOfParsing (SVG path \"${data.path}\")")
        return block(number)
    }

    /**
     * Request next two numbers from `data`
     *
     * @param T Type of result returned by `block`
     * @param data Data about the current state when reading the SVG path definition
     * @param block Block of code to which the read numbers are passed
     *
     * @return Result of calling `block`
     *
     * @throws IllegalArgumentException If the one of next two parts of the path is a command;
     * if one of next two parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private inline fun <T> requireTwoNumbers(data: Data, block: (first: Double, second: Double) -> T): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        var indexOfParsing = data.index
        val first = nextNumber(data)
            ?: throw IllegalArgumentException("Expected two numbers, but no one found starting at index $indexOfParsing (SVG path \"${data.path}\")")

        indexOfParsing = data.index
        val second = nextNumber(data)
            ?: throw IllegalArgumentException("Expected two numbers, but second not found starting at index $indexOfParsing (SVG path \"${data.path}\")")

        return block(first, second)
    }

    /**
     * Request next numbers from `data`
     *
     * @param T Type of result returned by `block`
     * @param data Data about the current state when reading the SVG path definition
     * @param count Number of numbers to read from SVG path definition
     * @param block The block of code into which the read numbers are passed
     *
     * @return Result of calling `block`
     *
     * @throws IllegalArgumentException If the one of next parts of the path is a command;
     * if one of next parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private inline fun <T> requireNumbers(data: Data, count: Int, block: (numbers: DoubleArray) -> T): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val numbers = DoubleArray(count) { index ->
            val indexOfParsing = data.index
            nextNumber(data)
                ?: throw IllegalArgumentException("Expected $count numbers, but ${index + 1}-th number not found starting at index $indexOfParsing (SVG path \"${data.path}\")")
        }
        return block(numbers)
    }
    // endregion


    /**
     * Start breakpoint calculation based on previous breakpoint used for
     * "S"/"s" (smooth cubic Bézier curve) and "T"/"t" (smooth quadratic Bézier curve) commands
     *
     * @param T Type of result returned by `block`
     * @param relative Used when `prevControlPoint` is null.
     * If equal to `true` `data`.[point][Data.point] is used as the reflection point of the previous checkpoint; otherwise - [ZERO_POINT] is used
     * @param data Data about the current state when reading the SVG path definition
     * @param prevControlPoint Previous control point
     * @param block The block to which the calculated coordinates of the new control point are transferred
     *
     * @return Result of calling `block`
     */
    private inline fun <T> controlPointUsingPrev(
        relative: Boolean,
        data: Data,
        prevControlPoint: Point2D.Double?,
        block: (controlX: Double, controlY: Double) -> T
    ): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val controlX: Double
        val controlY: Double
        if (prevControlPoint == null) {
            val relativePoint = if (relative) {
                data.point
            } else {
                ZERO_POINT
            }

            controlX = relativePoint.x
            controlY = relativePoint.y
        } else {
            val xDiff = data.point.x - prevControlPoint.x
            val yDiff = data.point.y - prevControlPoint.y
            controlX = data.point.x + xDiff
            controlY = data.point.y + yDiff
        }

        return block(controlX, controlY)
    }


    // region "Drawing" methods
    /**
     * Performs [MoveTo commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#moveto_path_commands) on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next two parts of the path is a command;
     * if one of next two parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private fun move(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireTwoNumbers(data) { x, y ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                result.moveTo(relativePoint.x + x, relativePoint.y + y)
            }
        }

        data.clearPrev()
    }

    /**
     * Performs [LineTo commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#lineto_path_commands) ("L" and "l") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next two parts of the path is a command;
     * if one of next two parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private fun line(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireTwoNumbers(data) { x, y ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                result.lineTo(relativePoint.x + x, relativePoint.y + y)
            }
        }

        data.clearPrev()
    }

    /**
     * Performs [LineTo commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#lineto_path_commands) ("H" and "h") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the next part of the path is a command;
     * if next part of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private fun hLine(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumber(data) { x ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }
                val y = data.point.y

                result.lineTo(relativePoint.x + x, y)
            }
        }

        data.clearPrev()
    }

    /**
     * Performs [LineTo commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#lineto_path_commands) ("V" and "v") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the next part of the path is a command;
     * if next part of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private fun vLine(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumber(data) { y ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }
                val x = data.point.x

                result.lineTo(x, relativePoint.y + y)
            }
        }

        data.clearPrev()
    }

    /**
     * Performs [cubic Bézier commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#cubic_b%C3%A9zier_curve) ("C" and "c") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next 6 parts of the path is a command;
     * if one of next 6 parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    @Suppress("DuplicatedCode")
    private fun cubicBezier(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumbers(data, 6) { numbers ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                val x1 = (relativePoint.x + numbers[0])
                val y1 = (relativePoint.y + numbers[1])
                val x2 = (relativePoint.x + numbers[2])
                val y2 = (relativePoint.y + numbers[3])
                val x = (relativePoint.x + numbers[4])
                val y = (relativePoint.y + numbers[5])

                data.prevCubicBezierPoint = Point2D.Double(x2, y2)

                result.curveTo(x1, y1, x2, y2, x, y)
            }
        }

        data.clearPrev(cubicBezier = false)
    }

    /**
     * Performs [smooth cubic Bézier commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#cubic_b%C3%A9zier_curve) ("S" and "s") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next 4 parts of the path is a command;
     * if one of next 4 parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    @Suppress("DuplicatedCode")
    private fun smoothCubicBezier(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumbers(data, 4) { numbers ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                val x1: Double
                val y1: Double
                controlPointUsingPrev(relative, data, data.prevCubicBezierPoint) { controlX, controlY ->
                    x1 = controlX
                    y1 = controlY
                }

                val x2 = (relativePoint.x + numbers[0])
                val y2 = (relativePoint.y + numbers[1])

                val x = (relativePoint.x + numbers[2])
                val y = (relativePoint.y + numbers[3])

                data.prevCubicBezierPoint = Point2D.Double(x2, y2)

                result.curveTo(x1, y1, x2, y2, x, y)
            }
        }

        data.clearPrev(cubicBezier = false)
    }

    /**
     * Performs [quadratic Bézier commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#quadratic_b%C3%A9zier_curve) ("Q" and "q") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next 4 parts of the path is a command;
     * if one of next 4 parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    @Suppress("DuplicatedCode")
    private fun quadraticBezier(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumbers(data, 4) { numbers ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                val x1 = (relativePoint.x + numbers[0])
                val y1 = (relativePoint.y + numbers[1])
                val x = (relativePoint.x + numbers[2])
                val y = (relativePoint.y + numbers[3])

                data.prevQuadraticBezierPoint = Point2D.Double(x1, y1)

                result.quadTo(x1, y1, x, y)
            }
        }

        data.clearPrev(quadraticBezier = false)
    }

    /**
     * Performs [smooth quadratic Bézier commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#quadratic_b%C3%A9zier_curve) ("T" and "t") on `result`
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next 2 parts of the path is a command;
     * if one of next 2 parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    @Suppress("DuplicatedCode")
    private fun smoothQuadraticBezier(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireTwoNumbers(data) { lx, ly ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                val x1: Double
                val y1: Double
                controlPointUsingPrev(relative, data, data.prevQuadraticBezierPoint) { controlX, controlY ->
                    x1 = controlX
                    y1 = controlY
                }

                val x = (relativePoint.x + lx)
                val y = (relativePoint.y + ly)

                data.prevQuadraticBezierPoint = Point2D.Double(x1, y1)

                result.quadTo(x1, y1, x, y)
            }
        }

        data.clearPrev(quadraticBezier = false)
    }

    /**
     * Performs [elliptical arc curve commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#elliptical_arc_curve) ("A" and "a") on `result`
     *
     * Uses code copied from __The Apache™ Batik Project__ - see [OreSwingExtJava.computeArc]
     *
     * @param relative If equal to `true`, then command coordinates are relative; otherwise - absolute
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     *
     * @throws IllegalArgumentException If the one of next 7 parts of the path is a command;
     * if one of next 7 parts of the path cannot be read as a number;
     * if reading the path is already complete
     */
    private fun arc(relative: Boolean, data: Data, result: Path2D.Double) {
        while (nextToken(data) == Token.NUMBER) {
            requireNumbers(data, 7) { numbers ->
                val relativePoint = if (relative) {
                    data.point
                } else {
                    ZERO_POINT
                }

                val rx = numbers[0]
                val ry = numbers[1]
                val angle = numbers[2]
                val largeArcFlag = (numbers[3] != 0.0)
                val sweepFlag = (numbers[4] != 0.0)
                val x = (relativePoint.x + numbers[5])
                val y = (relativePoint.y + numbers[6])

                val x0: Double
                val y0: Double
                data.point.let { dataPoint ->
                    x0 = dataPoint.x
                    y0 = dataPoint.y
                }

                val arc = OreSwingExtJava.computeArc(x0, y0,rx, ry, angle, largeArcFlag, sweepFlag, x, y)
                    ?: error("Created arc is NULL (unexpectedly)")

                val rotatedArc = AffineTransform.getRotateInstance(angle, arc.x + (arc.width / 2), arc.y + (arc.height / 2))
                    .createTransformedShape(arc)
                    ?: error("Rotated arc is NULL (unexpectedly)")

                result.append(rotatedArc, true)
            }
        }

        data.clearPrev()
    }

    /**
     * Performs [ClosePath commands](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#closepath) ("Z" and "z") on `result`
     *
     * @param data Data about the current state when reading the SVG path definition
     * @param result Complemented path; will be returned by the [parse] method
     */
    private fun close(data: Data, result: Path2D.Double) {
        result.closePath()
        data.clearPrev()
    }
    // endregion


    /**
     * Reading a path based on the SVG path definition passed to `path`
     *
     * _More about SVG path definition - [at https://developer.mozilla.org/](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d)_
     *
     * @param path Text to be read as SVG path definition
     * @param leaveIncompleteOnError If it is `true`, then on a read error it does not throw it away, but leaves the created path unfinished,
     * caches it (if `cache` equals to `true`) and returns it; default is `false`
     * @param cache If equal to `true`, then store the path read from `path` in the cache; default - [useCache]
     *
     * @return Path read from `path`
     *
     * @throws IllegalArgumentException On various `path` read errors
     */
    fun parse(path: String, leaveIncompleteOnError: Boolean = false, cache: Boolean = useCache): Path2D.Double {
        val stored = getFromCache(path, leaveIncompleteOnError)
        if (stored != null) {
            val exception = stored.second
            return when {
                (exception == null) -> stored.first
                (leaveIncompleteOnError) -> stored.first
                else -> throw exception
            }
        }

        val result = Path2D.Double()
        result.moveTo(0.0, 0.0)

        Data(path = path.trim(), index = 0, result = result).let { data ->
            try {
                while (true) {
                    val indexOfParsing = data.index
                    val command = nextCommand(data)
                        ?: break

                    when (command) {
                        'M' -> move(false, data, result)
                        'm' -> move(true, data, result)
                        'L' -> line(false, data, result)
                        'l' -> line(true, data, result)
                        'H' -> hLine(false, data, result)
                        'h' -> hLine(true, data, result)
                        'V' -> vLine(false, data, result)
                        'v' -> vLine(true, data, result)
                        'C' -> cubicBezier(false, data, result)
                        'c' -> cubicBezier(true, data, result)
                        'S' -> smoothCubicBezier(false, data, result)
                        's' -> smoothCubicBezier(true, data, result)
                        'Q' -> quadraticBezier(false, data, result)
                        'q' -> quadraticBezier(true, data, result)
                        'T' -> smoothQuadraticBezier(false, data, result)
                        't' -> smoothQuadraticBezier(true, data, result)
                        'A' -> arc(false, data, result)
                        'a' -> arc(true, data, result)
                        'Z' -> close(data, result)
                        'z' -> close(data, result)
                        else -> throw IllegalArgumentException("Unknown command \"$command\" starting at index $indexOfParsing (SVG path \"${data.path}\")")
                    }
                }

                if (cache) cache(path, result)
            } catch (e: Exception) {
                if (cache) cache(path, result, e)

                if (leaveIncompleteOnError) {
                    OreSwingExt.handle(e)
                } else {
                    throw e
                }
            }
        }

        return result
    }
}
