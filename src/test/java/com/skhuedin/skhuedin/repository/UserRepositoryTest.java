package com.skhuedin.skhuedin.repository;

import com.skhuedin.skhuedin.domain.Provider;
import com.skhuedin.skhuedin.domain.User;
import com.skhuedin.skhuedin.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@Transactional
@Rollback(value = false)
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;


    @Test
    public void testUser() {


        User user = new User(null, "her0807@naver.com",
                "1234", Provider.self, "host0807");
        User user1 = new User(null, "hello@naver.com",
                "1234", Provider.self, "host0807");
        System.out.println(user.getEmail());
        userService.join(user);
        userService.join(user1);



    }


}