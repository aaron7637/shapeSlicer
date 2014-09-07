package com.extremefruitslicer.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Class that represents a Fruit. Can be split into two separate fruits.
 */
public class Fruit extends View {
    private Path path = new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix transform = new Matrix();

    private float xVelocity = 130;
    private float yVelocity = -250;
    private static float acceleration = 2000;
    private Boolean isSplit = false;

    /**
     * A fruit is represented as Path, typically populated
     * by a series of points
     */

    public Fruit(Context context) {
        super(context);
    }


    Fruit(float[] points, Context context) {
        super(context);
        init();
        this.path.reset();
        this.path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            this.path.lineTo(points[i], points[i + 1]);
        }
        this.path.moveTo(points[0], points[1]);
    }

    Fruit(Region region, Context context) {
        super(context);
        init();
        this.path = region.getBoundaryPath();
    }

    Fruit(Path path, Context context) {
        super(context);
        init();
        this.path = path;
    }

    public Fruit clone() {
        Path copyArea = new Path(path);
        Fruit copy = new Fruit(copyArea, this.getContext());
        copy.paint = paint;
        copy.transform = new Matrix(transform);
        return copy;
    }

    private void init() {
        this.paint.setColor(Color.BLUE);
        this.paint.setStrokeWidth(5);
    }

    /**
     * The color used to paint the interior of the Fruit.
     */
    public int getFillColor() {
        return paint.getColor();
    }

    public void setFillColor(int color) {
        paint.setColor(color);
    }

    /**
     * Concatenates transforms to the Fruit's affine transform
     */
    public void rotate(float theta) {
        transform.postRotate(theta);
    }

    public void scale(float x, float y) {
        transform.postScale(x, y);
    }

    public void translate(float tx, float ty) {
        transform.postTranslate(tx, ty);
    }

    /**
     * The path used to describe the fruit shape.
     */
    public Path getTransformedPath() {
        Path originalPath = new Path(path);
        Path transformedPath = new Path();
        originalPath.transform(transform, transformedPath);
        return transformedPath;
    }

    public RectF getFBounds() {
        RectF bounds = new RectF();
        getTransformedPath().computeBounds(bounds, true);
        return bounds;
    }

    /**
     * Paints the Fruit to the screen using its current affine
     */
    public void draw(Canvas canvas) {
        canvas.drawPath(getTransformedPath(), paint);
    }

    /**
     * Tests whether the line represented by the two points intersects
     * this Fruit.
     */
    public boolean intersects(PointF p1, PointF p2) {

        if (isSplit) return false;

        Path line = new Path();

        line.moveTo(p1.x, p1.y - 1);
        line.lineTo(p2.x, p2.y - 1);
        line.lineTo(p2.x, p2.y + 1);
        line.lineTo(p1.x, p1.y + 1);
        line.moveTo(p1.x, p1.y - 1);

        Path transformedPath = getTransformedPath();

        RectF fclip = new RectF();
        Rect clip = new Rect();
        transformedPath.computeBounds(fclip, true);
        clip.set((int) fclip.left, (int) fclip.top, (int) fclip.right, (int) fclip.bottom);
        //clip.set(0, 0, 1000, 1000);

        Region fruitRegion = new Region();
        Region lineRegion = new Region();
        Region clipRegion = new Region(clip);

        fruitRegion.setPath(line, clipRegion);
        lineRegion.setPath(line, clipRegion);

        return fruitRegion.op(lineRegion, Region.Op.INTERSECT);

    }

    public boolean isSplitPiece() {
        return isSplit;
    }

    /**
     * Returns whether the given point is within the Fruit's shape.
     */
    public boolean contains(PointF p1) {
        Region region = new Region();
        boolean valid = region.setPath(getTransformedPath(), new Region());
        return valid && region.contains((int) p1.x, (int) p1.y);
    }

    public void translateFruit() {
        yVelocity = yVelocity + acceleration * (float) 0.01;
        float deltaX;
        deltaX = (float) 0.01 * xVelocity;
        translate(deltaX, (float) 0.01 * yVelocity);

        if (getFBounds().top <= 0) {
            yVelocity = -yVelocity;
        }

        if (getFBounds().left <= 0 || getFBounds().right >= MainActivity.displaySize.x) {
            xVelocity = -xVelocity;
        }
    }

    public void setVelocity(float xVelocity, float yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    public static void incrementAcceleration() {
        acceleration += (float) 0.1;
    }

    /**
     * This method assumes that the line represented by the two points
     * intersects the fruit. If not, unpredictable results will occur.
     * Returns two new Fruits, split by the line represented by the
     * two points given.
     */
    public Fruit[] split(PointF p1, PointF p2) {

        float angle = (float) Math.atan2(p2.y - p1.y, p2.x - p1.x) * 180 / (float) Math.PI;

        // Create transforms for rotating about p1 and undoing that operation
        Matrix myTransform = new Matrix();
        Matrix myInverseTransform = new Matrix();
        myTransform.setRotate(-1 * angle, p1.x, p1.y);
        myInverseTransform.setRotate(angle, p1.x, p1.y);

        Region clipRegion = new Region(0, 0, 10000, 10000);

        // Get the transformed Path
        Region fruitShapeRegion = new Region();
        Path fruitShapeCopy = getTransformedPath();
        fruitShapeCopy.transform(myTransform);
        fruitShapeRegion.setPath(fruitShapeCopy, clipRegion);

        // Generate the Regions below and above the y-axis with p1 at the origin
        Path topAreaPath = new Path();
        Region topArea = new Region();
        Path bottomAreaPath = new Path();
        Region bottomArea = new Region();

        topAreaPath.addRect(-10000, (int) p1.y - 10000, 10000, (int) p1.y, Path.Direction.CW);
        topArea.setPath(topAreaPath, clipRegion);
        bottomAreaPath.addRect(-10000, (int) p1.y, 10000, (int) p1.y + 10000, Path.Direction.CW);
        bottomArea.setPath(bottomAreaPath, clipRegion);

        // Find the intersection of the top and bottom halves with the fruit
        topArea.op(fruitShapeRegion, Region.Op.INTERSECT);
        bottomArea.op(fruitShapeRegion, Region.Op.INTERSECT);

        Fruit[] fruitArray = new Fruit[]{new Fruit(topArea, getContext()), new Fruit(bottomArea, getContext())};

        fruitArray[0].path.transform(myInverseTransform);
        fruitArray[1].path.transform(myInverseTransform);

        fruitArray[0].isSplit = true;
        fruitArray[1].isSplit = true;
        fruitArray[0].paint = new Paint(this.paint);
        fruitArray[1].paint = new Paint(this.paint);
        fruitArray[0].yVelocity = Math.abs(yVelocity * 2);
        fruitArray[1].yVelocity = Math.abs(yVelocity * 2);
        fruitArray[0].xVelocity = -1 * xVelocity;
        fruitArray[1].xVelocity = xVelocity;

        if (topArea != null && bottomArea != null) return fruitArray;
        else return new Fruit[1];
    }
}
