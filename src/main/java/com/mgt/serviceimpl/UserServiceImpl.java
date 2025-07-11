package com.mgt.serviceimpl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.model.Role;
import com.mgt.model.User;
import com.mgt.repository.UserRepo;

@Service
public class UserServiceImpl {

	@Autowired
	private UserRepo userRepo;

	public User addUser(User user) {

		return userRepo.save(user);
	}

/* 	public User loginUserEmail(String email, String password) {
		User validUser = userRepo.findByEmail(email);
		if (validUser != null && validUser.getPassword().matches(password)) {
			return validUser;
		}
		return null;
	} */

	public boolean deleteUser(Long id) {

		boolean status = false;
		if (id != null) {
			userRepo.deleteById(id);
			status = true;
		}
		return status;
	}
//
//	public User updatUser(User user) {
//		Optional<User> existingUser = UserRepo.findById(user.getId());
//		if(existingUser.isPresent()){
//			return userRepo.save(user);
//		}else{
//			return null;
//		}
//	}
	public User findById(Long id) {
    Optional<User> user = userRepo.findById(id);
    return user.orElse(null);
}


	public List<User> getAllUser() {

		return userRepo.findByRole(Role.USER);
	}


}
