package opticalflow;

import javax.swing.JOptionPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

public class OpticalFlow extends Thread
{
    static
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java451");
    }
    
    private VideoCapture camara;
    private boolean ejecutar = true;
    private Mat primerFrame;
    private MatOfPoint2f primerasEsquinas;
    private Mat nuevoFrame;
    private MatOfPoint2f nuevasEsquinas;
    private MatOfByte status;
    private boolean movimiento = false;
    private long tiempo;
    
    public OpticalFlow(){}
    
    @Override
    public void run()
    {
        camara = new VideoCapture(1);
        primerFrame = obtenerFrame(camara);
        primerasEsquinas = obtenerEsquinas(primerFrame);
        tiempo = System.currentTimeMillis();
      
        while(ejecutar)
        {
            if(camara.isOpened())
            {
                nuevoFrame = obtenerFrame(camara);
                nuevasEsquinas = obtenerEsquinas(nuevoFrame);
                status = calcularFO(primerFrame, primerasEsquinas, nuevoFrame, nuevasEsquinas);
                
                for(int i = 0; i < status.toArray().length && !movimiento; ++i)
                {
                    if(status.toArray()[i] == 0)
                    {
                        movimiento = true;
                        System.out.println("MOVIMIENTO");
                    }
                }
                movimiento = false;
                if(System.currentTimeMillis() - tiempo <= 900000)
                {
                    tiempo = System.currentTimeMillis();
                    primerFrame = obtenerFrame(camara);
                    primerasEsquinas = obtenerEsquinas(primerFrame);
                }
            }
            else
            {
                ejecutar = false;
                JOptionPane.showMessageDialog(null,"No es posible acceder a la cámara."); 
            }
        }  
    }
    
    public Mat obtenerFrame(VideoCapture camara)
    {
        Mat primerFrame = new Mat();
        camara.read(primerFrame);
        Imgproc.cvtColor(primerFrame, primerFrame, Imgproc.COLOR_BGR2GRAY);
        return primerFrame;
    }
    
    public MatOfPoint2f obtenerEsquinas(Mat frame)
    {
        MatOfPoint esquinas = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(frame, esquinas, 100, 0.3, 7, new Mat(), 7, false, 0.04);
        MatOfPoint2f esquinas2f = new MatOfPoint2f();
        esquinas2f = new MatOfPoint2f(esquinas.toArray());
        return esquinas2f;
    }
    
    public MatOfByte calcularFO(Mat primerFrame, MatOfPoint2f primerasEsquinas, Mat nuevoFrame, MatOfPoint2f nuevasEsquinas)
    {
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 10, 0.03);
        Video.calcOpticalFlowPyrLK(primerFrame, nuevoFrame, primerasEsquinas, nuevasEsquinas, status, err, new Size(15,15),2, criteria);
        return status;
    }
    
    public static void main(String[] args)
    {
        OpticalFlow of = new OpticalFlow();
        of.run();
    }
}
