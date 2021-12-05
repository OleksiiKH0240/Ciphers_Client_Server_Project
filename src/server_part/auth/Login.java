package server_part.auth;

import server_part.MultiThreadServer;

public class Login {
    static public String loginPerson(String name, String password){
        String response;
        String res = MultiThreadServer.getSPersonData().getPersonPasswordByName(name);
        if (res.equals("")){
            response = "person with such name does not exist";
        }
        else if (res.equals("FileNotExist")){
            response = "Login Failed";
        }
        else{
            // return password case
            if (res.equals(password)){
                response = "Successfully Logged In";
            }
            else{
                response = "Login Failed";
            }
        }
        return response;
    }
}
