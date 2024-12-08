package edu.upc.dsa;

import edu.upc.dsa.exceptions.*;
import edu.upc.dsa.models.User;

import java.util.List;

public interface UserManager {

    public User addUser(String user, String password, String mail) throws UserRepeatedException;
    //Implementada
    public User addUser(User u) throws UserRepeatedException;
    //Implementada
    public User getUserFromUsername(String _username) throws  UserNotFoundException;
    //Implementada
    public List<User> findAll();
    //Implementada
    public void deleteUser(String userName) throws UserNotFoundException;
    //Implementada
    public User updateUser(User u)throws UserNotFoundException;
    //Implementada
    public void updateCobre(double cobre, User user)throws UserNotFoundException;
    //Implementada
    public double updateMoney(User user, double kilocobre) throws UserNotEnoughCobreException, UserHasNoMultiplicadorException;
    //Implementada
    public double damePrecioCobre(User user);
    //Implementada (No fa res a la BD)
    public void clear();
    //Implementada
    public int size();
    //Implementada
    public void changePassword(User user, String pswd);
    //Implementada
    public void RecoverPassword(User user) throws Exception;
    //Implementada
}
