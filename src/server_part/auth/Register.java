package server_part.auth;

import server_part.MultiThreadServer;

public class Register {
    static public String registerPerson(String name, String password) {
        String response = "";
        int res = MultiThreadServer.getSPersonData().savePerson(name, password);
        if (res == 0) {
            response = "person was registered";
        } else if (res == 1) {
            response = "person with such name was existed, no need to register";
        } else if (res == -1) {
            response = "person was not saved, something went wrong";
        }

        return (response.equals("")) ? "something went wrong" : response;
    }
}
