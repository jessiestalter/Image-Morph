import java.awt.*;

public class ControlTriangle {

    // initialize the control points used to define the triangle
    private ControlPoint c1, c2, c3;

    public Shape triangle; // shape representing the triangle

    // constructor
    public ControlTriangle(ControlPoint givenC1, ControlPoint givenC2, ControlPoint givenC3) {
        c1 = givenC1;
        c2 = givenC2;
        c3 = givenC3;

        triangle = new Polygon(new int[] {c1.x,c2.x,c3.x}, new int[] {c1.y,c2.y,c3.y}, 3);
    }

    // functions to get the control points
    public ControlPoint getC1() {
        return c1;
    }
    public ControlPoint getC2() {
        return c2;
    }
    public ControlPoint getC3() {
        return c3;
    }
}
