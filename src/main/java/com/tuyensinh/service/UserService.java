package com.tuyensinh.service;

import java.util.List;

import com.tuyensinh.entity.User;
import com.tuyensinh.entity.UserRole;
import com.tuyensinh.repository.UserRepository;

public class UserService {

    private UserRepository userRepository;

    public UserService() {
        userRepository = new UserRepository();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Object[]> getAllUsersForTable() {
        return userRepository.findAllForTable();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User loginAdmin(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập username và mật khẩu.");
        }

        User user = userRepository.findByUsername(username.trim());
        if (user == null) {
            throw new RuntimeException("Tài khoản không tồn tại.");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mật khẩu không đúng.");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Tài khoản đã bị khóa.");
        }

        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Tài khoản không có quyền truy cập hệ thống admin.");
        }

        return user;
    }

    public boolean addAdmin(String username, String password, boolean enabled) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập đầy đủ thông tin.");
        }

        User existed = userRepository.findByUsername(username.trim());
        if (existed != null) {
            throw new RuntimeException("Username đã tồn tại.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole(UserRole.ADMIN);
        user.setEnabled(enabled);
        user.setThisinhId(null);

        userRepository.save(user);
        return true;
    }

    public boolean updateUser(Integer id, String username, boolean enabled) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username không được để trống.");
        }

        User existed = userRepository.findByUsername(username.trim());
        if (existed != null && !existed.getId().equals(id)) {
            throw new RuntimeException("Username đã tồn tại.");
        }

        user.setUsername(username.trim());
        user.setEnabled(enabled);

        userRepository.update(user);
        return true;
    }

    public boolean updateRole(Integer id, String role) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng.");
        }

        try {
            user.setRole(UserRole.valueOf(role));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Quyền không hợp lệ.");
        }

        userRepository.update(user);
        return true;
    }

    public boolean toggleStatus(Integer id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng.");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.update(user);
        return true;
    }

    public boolean changePassword(Integer id, String newPassword) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng.");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu mới không được để trống.");
        }

        user.setPassword(newPassword);
        userRepository.update(user);
        return true;
    }
}