package com.wildma.idcardcamera.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
 
/** Clase encargada de crear la capa que cubre a la imagen para recortarla
 * agrega puntos de referencia para realizar el recorte
 * brinda la opcion de aceptar o negar el recorte de la imagen para posteriormente utilizarla
 */

public class CropOverlayView extends View {

  // region: Fields and Consts
    private int defaultMargin = 100;
    private int minDistance = 100;
    private int vertexSize = 30;
    private int gridSize = 3;

    private Bitmap bitmap;
    private Point topLeft, topRight, bottomLeft, bottomRight;

    private float touchDownX, touchDownY;
    private CropPosition cropPosition;

    private int currentWidth = 0;
    private int currentHeight = 0;

    private int minX, maxX, minY, maxY;
// endregion
    /**Constructores del CropOverlayView
     * Utilice getContext ()
     * */
    public CropOverlayView(Context context) {
        super(context);
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**Metodo para asignar el bitmap (foto capturada) al overlay
     * y resetear los puntos de referencia
     * Invocar al metodo invalidate() de View para repintar la pantalla */

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        resetPoints();
        invalidate();
    }

    /** Metodo para dibujar el fondo difuminado
     * los vertices, las lineas y la cuadricula de referencia para recortar la imagen
     * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //obtener las medidas actuales
        if (getWidth() != currentWidth || getHeight() != currentHeight) {
            currentWidth = getWidth();
            currentHeight = getHeight();
            resetPoints();
        }
        drawBackground(canvas); //Dibujar el fondo difuminado
        drawVertex(canvas);     //Dibujar los vertices en el canva
        drawEdge(canvas);       //Dibujar las lineas que unen los vertices
        drawGrid(canvas);       //Dibujar una cuadricula de referencia
    }

    /**Metodo para resetear los puntos de referencia para realizar el recorte de la
     * imagen
     * */
    private void resetPoints() {

        // 1. calcular el tamaño del mapa de bits (la fotografia) en un nuevo canva
        float scaleX = bitmap.getWidth() * 1.0f / getWidth();
        float scaleY = bitmap.getHeight() * 1.0f / getHeight();
        float maxScale = Math.max(scaleX, scaleY);

        // 2. determinar si la fotografia es muy larga o ancha
        int minX = 0;
        int maxX = getWidth();
        int minY = 0;
        int maxY = getHeight();

        if (maxScale == scaleY) { // fotografia muy alta
            int bitmapInCanvasWidth = (int) (bitmap.getWidth() / maxScale);
            minX = (getWidth() - bitmapInCanvasWidth) / 2;
            maxX = getWidth() - minX;
        } else { // fotografia muy ancha
            int bitmapInCanvasHeight = (int) (bitmap.getHeight() / maxScale);
            minY = (getHeight() - bitmapInCanvasHeight)/2;
            maxY = getHeight() - minY;
        }

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        if (maxX - minX < defaultMargin || maxY - minY < defaultMargin)
            defaultMargin = 0; // remover el minimo
        else
            defaultMargin = 30;

/**Dibujar los 4 nuevos puntos segun el calculo
 * agregandole un margen de 100
 * */
        topLeft = new Point(minX + defaultMargin, minY + defaultMargin);
        topRight = new Point(maxX - defaultMargin, minY + defaultMargin);
        bottomLeft = new Point(minX + defaultMargin, maxY - defaultMargin);
        bottomRight = new Point(maxX - defaultMargin, maxY - defaultMargin);
    }

    /**Se llama para determinar los requisitos de tamaño para esta vista
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    /** Metodo que ayuda a dibujar el fondo difuminado de color gris
     * ayuda a dar un mejor encuadre de la credencial y asi obtener
     * una mejor fotografia
     * */
    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#66000000"));
        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.moveTo(topLeft.x, topLeft.y);
        path.lineTo(topRight.x, topRight.y);
        path.lineTo(bottomRight.x, bottomRight.y);
        path.lineTo(bottomLeft.x, bottomLeft.y);
        path.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#66000000"));
        canvas.restore();
    }

    /** Metodo para dibujar puntos en los vertices del rectangulo
     * ayuda a tomarlos como puntos de referencia al recortar la imagen
     * */
    private void drawVertex(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(topLeft.x, topLeft.y, vertexSize, paint);
        canvas.drawCircle(topRight.x, topRight.y, vertexSize, paint);
        canvas.drawCircle(bottomLeft.x, bottomLeft.y, vertexSize, paint);
        canvas.drawCircle(bottomRight.x, bottomRight.y, vertexSize, paint);


    }

    /**Metodo que ayuda a unir los vertices con una linea entre ellos
     *
     */

    private void drawEdge(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);

        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, paint);
        canvas.drawLine(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y, paint);
        canvas.drawLine(bottomRight.x, bottomRight.y, topRight.x, topRight.y, paint);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, paint);
    }
    /**Metodo que ayuda a crear una cuadricula entre los vertices de ayuda
     * se crea una cuadricula de 4x4
     * */
    private void drawGrid(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);

        for (int i = 1; i <= gridSize; i++) {
            int topDistanceX = Math.abs(topLeft.x - topRight.x) / (gridSize + 1) * i;
            int topDistanceY = Math.abs((topLeft.y - topRight.y) / (gridSize + 1) * i);

            Point top = new Point(
                    topLeft.x < topRight.x ? topLeft.x + topDistanceX : topLeft.x - topDistanceX,
                    topLeft.y < topRight.y ? topLeft.y + topDistanceY : topLeft.y - topDistanceY);

            int bottomDistanceX = Math.abs((bottomLeft.x - bottomRight.x) / (gridSize + 1) * i);
            int bottomDistanceY = Math.abs((bottomLeft.y - bottomRight.y) / (gridSize + 1) * i);
            Point bottom = new Point(
                    bottomLeft.x < bottomRight.x ? bottomLeft.x + bottomDistanceX : bottomLeft.x - bottomDistanceX,
                    bottomLeft.y < bottomRight.y ? bottomLeft.y + bottomDistanceY : bottomLeft.y - bottomDistanceY);

            canvas.drawLine(top.x, top.y, bottom.x, bottom.y, paint);

            int leftDistanceX = Math.abs((topLeft.x - bottomLeft.x) / (gridSize + 1) * i);
            int leftDistanceY = Math.abs((topLeft.y - bottomLeft.y) / (gridSize + 1) * i);

            Point left = new Point(
                    topLeft.x < bottomLeft.x ? topLeft.x + leftDistanceX : topLeft.x - leftDistanceX,
                    topLeft.y < bottomLeft.y ? topLeft.y + leftDistanceY : topLeft.y - leftDistanceY);

            int rightDistanceX = Math.abs((topRight.x - bottomRight.x) / (gridSize + 1) * i);
            int rightDistanceY = Math.abs((topRight.y - bottomRight.y) / (gridSize + 1) * i);

            Point right = new Point(
                    topRight.x < bottomRight.x ? topRight.x + rightDistanceX : topRight.x - rightDistanceX,
                    topRight.y < bottomRight.y ? topRight.y + rightDistanceY : topRight.y - rightDistanceY);

            canvas.drawLine(left.x, left.y, right.x, right.y, paint);
        }

    }

    /**Metodo de View para capturar los eventos en el touch */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(false);
                onActionDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                onActionMove(event);
                return true;
        }
        return false;
    }

    private void onActionDown(MotionEvent event) {
        touchDownX = event.getX();
        touchDownY = event.getY();
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        int minDistance = distance(touchPoint, topLeft);
        cropPosition = CropPosition.TOP_LEFT;
        if (minDistance > distance(touchPoint, topRight)) {
            minDistance = distance(touchPoint, topRight);
            cropPosition = CropPosition.TOP_RIGHT;
        }
        if (minDistance > distance(touchPoint, bottomLeft)) {
            minDistance = distance(touchPoint, bottomLeft);
            cropPosition = CropPosition.BOTTOM_LEFT;
        }
        if (minDistance > distance(touchPoint, bottomRight)) {
            minDistance = distance(touchPoint, bottomRight);
            cropPosition = CropPosition.BOTTOM_RIGHT;
        }
    }

    /**Obtener la distancia entre dos puntos*/
    private int distance(Point src, Point dst) {
        return (int) Math.sqrt(Math.pow(src.x - dst.x, 2) + Math.pow(src.y - dst.y, 2));
    }

    private void onActionMove(MotionEvent event) {
        int deltaX = (int) (event.getX() - touchDownX);
        int deltaY = (int) (event.getY() - touchDownY);

        switch (cropPosition) {
            case TOP_LEFT:
                adjustTopLeft(deltaX, deltaY);
                invalidate();
                break;
            case TOP_RIGHT:
                adjustTopRight(deltaX, deltaY);
                invalidate();
                break;
            case BOTTOM_LEFT:
                adjustBottomLeft(deltaX, deltaY);
                invalidate();
                break;
            case BOTTOM_RIGHT:
                adjustBottomRight(deltaX, deltaY);
                invalidate();
                break;
        }
        touchDownX = event.getX();
        touchDownY = event.getY();
    }

    private void adjustTopLeft(int deltaX, int deltaY) {
        int newX = topLeft.x + deltaX;
        if (newX < minX) newX = minX;
        if (newX > maxX) newX = maxX;

        int newY = topLeft.y + deltaY;
        if (newY < minY) newY = minY;
        if (newY > maxY) newY = maxY;

        topLeft.set(newX, newY);
    }

    private void adjustTopRight(int deltaX, int deltaY) {
        int newX = topRight.x + deltaX;
        if (newX > maxX) newX = maxX;
        if (newX < minX) newX = minX;

        int newY = topRight.y + deltaY;
        if (newY < minY) newY = minY;
        if (newY > maxY) newY = maxY;

        topRight.set(newX, newY);
    }

    private void adjustBottomLeft(int deltaX, int deltaY) {
        int newX = bottomLeft.x + deltaX;
        if (newX < minX) newX = minX;
        if (newX > maxX) newX = maxX;

        int newY = bottomLeft.y + deltaY;
        if (newY > maxY) newY = maxY;
        if (newY < minY) newY = minY;

        bottomLeft.set(newX, newY);
    }

    private void adjustBottomRight(int deltaX, int deltaY) {
        int newX = bottomRight.x + deltaX;
        if (newX > maxX) newX = maxX;
        if (newX < minX) newX = minX;

        int newY = bottomRight.y + deltaY;
        if (newY > maxY) newY = maxY;
        if (newY < minY) newY = minY;

        bottomRight.set(newX, newY);
    }

    /** Metodo que se encarga de realizar el recorte de la fotografia
     * tomando como parametros los puntos de referencia de la seleccion previa
     * creando un nuevo bitmap "stretch" el cual se le envia como onFinish al croplistener
     * */
    public void crop(CropListener cropListener, boolean needStretch) {
        if (topLeft == null) return;

        // calcular el tamañano del bitmap en un nuevo canvas
        float scaleX = bitmap.getWidth() * 1.0f / getWidth();
        float scaleY = bitmap.getHeight() * 1.0f / getHeight();
        float maxScale = Math.max(scaleX, scaleY);

        // volver a calcular las coordenadas en el mapa de bits original

        Point bitmapTopLeft = new Point((int) ((topLeft.x - minX) * maxScale), (int) ((topLeft.y - minY) * maxScale));
        Point bitmapTopRight = new Point((int) ((topRight.x - minX) * maxScale), (int) ((topRight.y - minY) * maxScale));
        Point bitmapBottomLeft = new Point((int) ((bottomLeft.x - minX) * maxScale), (int) ((bottomLeft.y - minY) * maxScale));
        Point bitmapBottomRight = new Point((int) ((bottomRight.x - minX) * maxScale), (int) ((bottomRight.y - minY) * maxScale));


        Bitmap output = Bitmap.createBitmap(bitmap.getWidth()+1, bitmap.getHeight()+1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();

        Path path = new Path();
        path.moveTo(bitmapTopLeft.x, bitmapTopLeft.y);
        path.lineTo(bitmapTopRight.x, bitmapTopRight.y);
        path.lineTo(bitmapBottomRight.x, bitmapBottomRight.y);
        path.lineTo(bitmapBottomLeft.x, bitmapBottomLeft.y);
        path.close();
        canvas.drawPath(path, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        Rect cropRect = new Rect(
                Math.min(bitmapTopLeft.x, bitmapBottomLeft.x),
                Math.min(bitmapTopLeft.y, bitmapTopRight.y),
                Math.max(bitmapBottomRight.x, bitmapTopRight.x),
                Math.max(bitmapBottomRight.y, bitmapBottomLeft.y));

        if(cropRect.width() <= 0 || cropRect.height() <= 0) { //用户裁剪的宽或高为0
            cropListener.onFinish(null);
            return;
        }
        Bitmap cut = Bitmap.createBitmap(
                output,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
        );

        if (!needStretch) {
            cropListener.onFinish(cut);
        } else {
            Point cutTopLeft = new Point();
            Point cutTopRight = new Point();
            Point cutBottomLeft = new Point();
            Point cutBottomRight = new Point();

            cutTopLeft.x = bitmapTopLeft.x > bitmapBottomLeft.x ? bitmapTopLeft.x - bitmapBottomLeft.x : 0;
            cutTopLeft.y = bitmapTopLeft.y > bitmapTopRight.y ? bitmapTopLeft.y - bitmapTopRight.y : 0;

            cutTopRight.x = bitmapTopRight.x > bitmapBottomRight.x ? cropRect.width() : cropRect.width() - Math.abs(bitmapBottomRight.x - bitmapTopRight.x);
            cutTopRight.y = bitmapTopLeft.y > bitmapTopRight.y ? 0 : Math.abs(bitmapTopLeft.y - bitmapTopRight.y);

            cutBottomLeft.x = bitmapTopLeft.x > bitmapBottomLeft.x ? 0 : Math.abs(bitmapTopLeft.x - bitmapBottomLeft.x);
            cutBottomLeft.y = bitmapBottomLeft.y > bitmapBottomRight.y ? cropRect.height() : cropRect.height() - Math.abs(bitmapBottomRight.y - bitmapBottomLeft.y);

            cutBottomRight.x = bitmapTopRight.x > bitmapBottomRight.x ? cropRect.width() - Math.abs(bitmapBottomRight.x - bitmapTopRight.x) : cropRect.width();
            cutBottomRight.y = bitmapBottomLeft.y > bitmapBottomRight.y ? cropRect.height() - Math.abs(bitmapBottomRight.y - bitmapBottomLeft.y) : cropRect.height();



            float width = cut.getWidth();
            float height = cut.getHeight();

            float[] src = new float[]{cutTopLeft.x, cutTopLeft.y, cutTopRight.x, cutTopRight.y, cutBottomRight.x, cutBottomRight.y, cutBottomLeft.x, cutBottomLeft.y};
            float[] dst = new float[]{0, 0, width, 0, width, height, 0, height};

            Matrix matrix = new Matrix();
            matrix.setPolyToPoly(src, 0, dst, 0, 4);
            Bitmap stretch = Bitmap.createBitmap(cut.getWidth(), cut.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas stretchCanvas = new Canvas(stretch);
//            stretchCanvas.drawBitmap(cut, matrix, null);
            stretchCanvas.concat(matrix);
            stretchCanvas.drawBitmapMesh(cut, WIDTH_BLOCK, HEIGHT_BLOCK, generateVertices(cut.getWidth(), cut.getHeight()), 0, null, 0, null);

            cropListener.onFinish(stretch);
        }
    }

    private int WIDTH_BLOCK = 40;
    private int HEIGHT_BLOCK = 40;

    private float[] generateVertices(int widthBitmap, int heightBitmap) {

        float[] vertices=new float[(WIDTH_BLOCK+1)*(HEIGHT_BLOCK+1)*2];

        float widthBlock = (float)widthBitmap/WIDTH_BLOCK;
        float heightBlock = (float)heightBitmap/HEIGHT_BLOCK;

        for(int i=0;i<=HEIGHT_BLOCK;i++)
            for(int j=0;j<=WIDTH_BLOCK;j++) {
                vertices[i * ((HEIGHT_BLOCK+1)*2) + (j*2)] = j * widthBlock;
                vertices[i * ((HEIGHT_BLOCK+1)*2) + (j*2)+1] = i * heightBlock;
            }
        return vertices;
    }


}
