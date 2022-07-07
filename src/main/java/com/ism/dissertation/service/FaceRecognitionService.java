package com.ism.dissertation.service;

import com.ism.dissertation.model.User;
import com.ism.dissertation.repository.UserRepo;
import lombok.extern.java.Log;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.opencv.core.*;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imdecode;

@Log
@Service
public class FaceRecognitionService {

    private final CascadeClassifier cascadeClassifier;

    @Autowired
    UserRepo userRepository;

    public FaceRecognitionService() {
//        System.out.printf("AICI -> java.library.path: %s%n", System.getProperty("java.library.path"));
        nu.pattern.OpenCV.loadShared();
        Loader.load(opencv_java.class);
        cascadeClassifier = new CascadeClassifier();
        cascadeClassifier.load("./src/main/resources/files/haarcascade_frontalface_alt.xml");
    }

    public boolean hasFace(String picture) {
        return detectFace(picture) != null;
    }

    public Mat detectFace(String picture) {
        byte[] imgbytes = Base64.decodeBase64(picture);
        Mat image = imdecode(new MatOfByte(imgbytes), Imgcodecs.IMREAD_GRAYSCALE);
        Imgproc.resize(image, image, new Size(300, 300));

        MatOfRect facesDetected = new MatOfRect();

        int minFaceSize = Math.round(image.rows() * 0.1f);
        cascadeClassifier.detectMultiScale(image,
                facesDetected, 1.1, 3,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size()
        );

        Rect[] facesArray = facesDetected.toArray();
        Rect cropface = null;
        if (facesArray.length != 1) {
            return null;
        }
        for (Rect face : facesArray) {
            Imgproc.rectangle(image, new Point(face.x, face.y),
                    new Point(face.x + face.width, face.y + face.height),
                    new Scalar(0, 255, 0), 0);
            cropface = new Rect(face.x, face.y, face.width, face.height);

            Imgproc.rectangle(image, face.tl(), face.br(), new Scalar(0, 0, 255), 1);
        }

////        //TODO: in image se afla poza cu moaca detectata
//        imwrite("mihai.png", image);
//        imwrite("mihai_crop.png", new Mat(image, cropface));

        return new Mat(image, cropface);
    }

    public boolean faceRecognition(String photo) {
        List<User> userList = userRepository.findAll();
        ArrayList<Mat> photosMat = new ArrayList<>();
        ArrayList<Integer> listOfIds = new ArrayList<>();
        userList.forEach(user -> {
            Blob picture = user.getPicture();
            photosMat.add(detectFace(convertBlobToString(picture)));
            listOfIds.add(user.getId());
        });

        MatOfInt labels = new MatOfInt();
        labels.fromList(new ArrayList<>(listOfIds));
        LBPHFaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.train(photosMat, labels);

        int[] outLabel = new int[1];
        double[] outConf = new double[1];
        faceRecognizer.predict(detectFace(photo), outLabel, outConf);

        return (outConf[0] <= 60);
    }

//    public Mat base642Mat(String base64) {
//        Mat matImage = null;
//        Mat src = null;
//        byte[] imgbytes = DatatypeConverter.parseBase64Binary(base64);
//
//        src = Imgcodecs.imdecode(new MatOfByte(imgbytes), Imgcodecs.IMREAD_UNCHANGED);
//        Size scaleSize = new Size(300, 300);
//        matImage = new Mat();
//        Imgproc.resize(src, matImage, scaleSize);
//        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_BGR2GRAY);
//        return matImage;
//    }

    public String convertBlobToString(Blob blob) {
        if (blob == null) {
            return null;
        }

        byte[] bdata;
        String result = "";
        try {
            bdata = blob.getBytes(1, (int) blob.length());
            result = new String(bdata);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;

    }

}
