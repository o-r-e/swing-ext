package me.ore.swing.ext;


import java.awt.geom.Arc2D;


/**
 * Some utility methods, written in plain Java (not Kotlin)
 */
public class OreSwingExtJava {
    /**
     * This method is copy of "computeArc" method in file <b>org/apache/batik/ext/awt/geom/ExtendedGeneralPath.java</b>
     * which is part of <a href="https://xmlgraphics.apache.org/batik/">The Apache™ Batik Project</a>
     * (used version is "1.14")
     * <br>
     *
     * Used sources of <b>The Apache™ Batik Project</b> can be downloaded at <a href="https://xmlgraphics.apache.org/batik/download.html">this page</a>.
     *
     * <hr>
     *
     * This constructs an unrotated Arc2D from the SVG specification of an
     * Elliptical arc.  To get the final arc you need to apply a rotation
     * transform such as:
     * <br>
     *
     * <code>
     * AffineTransform.getRotateInstance(angle, arc.getX() + arc.getWidth() / 2, arc.getY() + arc.getHeight() / 2);
     * </code>
     *
     * @param x0 Current X coordinate of the path
     * @param y0 Current Y coordinate of the path
     * @param rx Radius on X-axis of the ellipse
     * @param ry Radius on Y-axis of the ellipse
     * @param angle Represents a rotation (in degrees) of the ellipse relative to the x-axis
     * @param largeArcFlag Allows to choose one of the large arc (<code>true</code>) or small arc (<code></code>)
     * @param sweepFlag Allows to choose one of the clockwise turning arc () or counterclockwise turning arc (0)
     * @param x New current X for the next command
     * @param y New current Y for the next command
     *
     * @return Computed arc
     */
    public static Arc2D computeArc(double x0, double y0,
                                   double rx, double ry,
                                   double angle,
                                   boolean largeArcFlag,
                                   boolean sweepFlag,
                                   double x, double y) {
        //
        // Elliptical arc implementation based on the SVG specification notes
        //

        // Compute the half distance between the current and the final point
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        // Convert angle from degrees to radians
        angle = Math.toRadians(angle % 360.0);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        //
        // Step 1 : Compute (x1, y1)
        //
        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        // Ensure radii are large enough
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;
        // check that radii are large enough
        double radiiCheck = Px1/Prx + Py1/Pry;
        if (radiiCheck > 0.99999) {  // don't cut it too close
            double radiiScale = Math.sqrt(radiiCheck) * 1.00001;
            rx = radiiScale * rx;
            ry = radiiScale * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
        sq = (sq < 0) ? 0 : sq;
        @SuppressWarnings("SpellCheckingInspection")
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        //
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        //
        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        //
        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        //
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;
        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0 : 1.0;
        double angleStart = Math.toDegrees(sign * Math.acos(p / n));

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if(!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        //
        // We can now build the resulting Arc2D in double precision
        //
        Arc2D.Double arc = new Arc2D.Double();
        arc.x = cx - rx;
        arc.y = cy - ry;
        arc.width = rx * 2.0;
        arc.height = ry * 2.0;
        arc.start = -angleStart;
        arc.extent = -angleExtent;

        return arc;
    }
}
