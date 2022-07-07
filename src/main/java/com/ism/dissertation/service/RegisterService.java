package com.ism.dissertation.service;

import com.ism.dissertation.model.User;
import com.ism.dissertation.repository.UserRepo;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;


@Service
public class RegisterService {

    @Autowired
    FaceRecognitionService faceRecognitionService;

    @Autowired
    UserRepo userRepository;

    public void registerUser(String username, String password, String email, String photo) throws SQLException {
        Blob dbPicture = new SerialBlob(photo.getBytes());

        List<Integer> newUserId = userRepository.findLastId();
        String encPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(newUserId == null || newUserId.isEmpty() ? 1 : newUserId.get(0) + 1,
                username, encPassword, email, dbPicture);

        userRepository.save(newUser);
    }


}
