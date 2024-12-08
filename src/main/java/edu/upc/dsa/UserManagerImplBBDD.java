package edu.upc.dsa;

import edu.upc.dsa.exceptions.*;
import edu.upc.dsa.models.User;
import edu.upc.dsa.orm.FactorySession;
import edu.upc.dsa.orm.SessionBD;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class UserManagerImplBBDD implements UserManager {
    private static UserManager instance;
    protected HashMap<String, Double> multiplicadors;
    final static Logger logger = Logger.getLogger(UserManagerImplBBDD.class);
    SessionBD sessionBD;
    private UserManagerImplBBDD() {
        this.multiplicadors = new HashMap<>();
        sessionBD = FactorySession.openSession();
    }

    public static UserManager getInstance() {
        if (instance==null) instance = new UserManagerImplBBDD();
        return instance;
    }

    public int size() {
        int ret = sessionBD.findAll(User.class).size();
        logger.info("size " + ret);

        return ret;
    }

    public User addUser(User u) throws UserRepeatedException {
        logger.info("new User " + u);
        if(sessionBD.get(u.getClass(),"correo",u.getCorreo()) == null)
        {
            sessionBD.save(u);
            logger.info("new User added");
            return u;
        }
        else{
            logger.warn("User already exists with that correu");
            throw new UserRepeatedException();
        }
    }

    public User addUser(String user, String password,  String mail) throws UserRepeatedException{
        return this.addUser(user, password, mail);
    }

    public User getUserFromUsername(String _username) throws UserNotFoundException{
        logger.info("getUser("+_username+")");
        User u = (User) sessionBD.get(User.class,"name", _username);
        if(u!= null) return u;
        logger.warn("not found " + _username);
        throw new UserNotFoundException();
    }


    public List<User> findAll() {
        return sessionBD.findAll(User.class);
    }

    @Override
    public void deleteUser(String userName) throws UserNotFoundException{
        User u = (User) sessionBD.get(User.class,"name", userName);
        if (u==null) {
            logger.warn("not found " + u);
        }
        else sessionBD.delete(User.class,"name",userName);
    }
    public void updateCobre(double cobre, User user)throws UserNotFoundException{
        user.setCobre(cobre + user.getCobre());
        this.updateUser(user);
    };
    public double updateMoney(User user, double kilocobre) throws UserNotEnoughCobreException, UserHasNoMultiplicadorException{
        if (multiplicadors.containsKey(user.getName())) {
            if(user.getCobre() >= kilocobre){
                double resultat = user.getMoney() + kilocobre*multiplicadors.get(user.getName());
                user.setMoney(resultat);
                user.setCobre(user.getCobre()-kilocobre);
                sessionBD.update(user,"correo",user.getCorreo());
                return resultat;
            }
            else throw new UserNotEnoughCobreException();
        }
        else throw new UserHasNoMultiplicadorException();
    };
    public double damePrecioCobre(User user){
        double random = Math.random();
        double multiplicador = 1 + Math.log(1 + (9 * random));
        double arrodonit = Math.round(multiplicador*10.0)/10.0;
        multiplicadors.put(user.getName(), arrodonit);
        return arrodonit;
    };


    @Override
    public User updateUser(User u) throws UserNotFoundException{
        User t = (User) sessionBD.get(u.getClass(),"correo",u.getCorreo());
        if (t!=null) {
            logger.info(u+" rebut!!!! ");
            sessionBD.update(u,"correo", u.getCorreo());
            logger.info(t+" updated ");
        }
        else {
            logger.warn("not found "+u);
        }
        return t;
    }
    public void clear() {
        sessionBD.deleteAll(User.class);
    }

    public void changePassword(User user, String pswd){
        user.setPassword(pswd);
        sessionBD.update(user,"correo",user.getCorreo());
    };
    public void RecoverPassword(User user) throws Exception{
        String host = "smtp.gmail.com";
        final String fromEmail = "correuperdsa@gmail.com";
        final String password = "eqqq ymrx grbg htld";
        User u1 = (User) sessionBD.get(user.getClass(),"correo", user.getCorreo());
        String toEmail = u1.getCorreo();
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        // Crear el missatge
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Recuperació de contrasenya Per RobaCobres");

        // Cos del text del correu
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Hola " + user.getName() + ",\n\nLa teva contrasenya és: " + user.getPassword() + "\n\nIntenta no tornara a oblidar-ho, olvidonaaa!");

        // Adjuntar imatge
        MimeBodyPart imagePart = new MimeBodyPart();
        File imageFile = new File("public/Olvidonaa.jpg");
        imagePart.attachFile(imageFile);
        imagePart.setFileName("Olvidonaa.jpg"); // Nom de l'arxiu al correu

        // Crear el contingut multipart
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart); // Afegir el text
        multipart.addBodyPart(imagePart); // Afegir la imatge

        // Assignar el contingut al missatge
        message.setContent(multipart);

        // Enviar el correu
        Transport.send(message);
    }

}