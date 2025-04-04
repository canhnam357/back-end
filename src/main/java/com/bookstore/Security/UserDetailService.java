package com.bookstore.Security;

import com.bookstore.Entity.User;
import com.bookstore.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = null;
        if(userRepository.findByEmailAndIsActiveIsTrue(email).isPresent()){
            user = userRepository.findByEmailAndIsActiveIsTrue(email)
                    .orElseThrow(()->new UsernameNotFoundException("user is not found1"));
        }
        return new UserDetail(user);
    }

    public UserDetails loadUserByUserId(String id){
        User user = userRepository.findByUserIdAndIsActiveIsTrue(id)
                .orElseThrow(()->new UsernameNotFoundException("user is not found"));
        return new UserDetail(user);
    }


}

