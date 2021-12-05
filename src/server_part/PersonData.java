package server_part;

import java.io.*;

public class PersonData {
    private String mFilePath;
    private File mFile;
    final private String SEP = ":";


    public PersonData() {
        String basePath = new File("").getAbsolutePath();
        mFile = new File(basePath + "/resource/PersonsFile.txt");
    }
    public PersonData(String filePath){
        mFilePath = filePath;
        mFile = new File(mFilePath);
    }

    public String getMFilePath() {
        return mFilePath;
    }

    /**
     *
     * @param name unique name of person
     * @return
     * password if all is ok;
     * empty String if Person with such name was not found;
     * 'FileNotExist' if file does not exist.
     */
    public String getPersonPasswordByName(String name) {
        String password = "";
        BufferedReader br;
        FileReader fr;
        try {
            fr = new FileReader(mFile);
            br = new BufferedReader(fr);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FileNotExist";
        }
        String[] tokens;
        for (String line : br.lines().toList()) {
            tokens = line.split(SEP);
            String currName = tokens[0];
            if (currName.equals(name)) {
                password = tokens[1];
                try {
                    fr.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return password;
            }
        }
        return password;
    }

    /**
     * @param name unique name of person
     * @param password password of person, that used to log in
     * @return
     * 0 if all is ok;
     * 1 if exist person in file with same name;
     * -1 if file does not exist.
     * Person fill one row in the file 'PersonsFile.txt', which looks like 'PersonName:PersonPassword' .
     */
    public int savePerson(String name, String password) {
        BufferedReader br;
        FileReader fr;
        try {
            fr = new FileReader(mFile);
            br = new BufferedReader(fr);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
        String[] tokens;
        for (String line : br.lines().toList()) {
             tokens = line.split(SEP);
             String currName = tokens[0];
             if (currName.equals(name)){
                 try {
                     br.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 try {
                     fr.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return 1;
             }
        }

        FileWriter fw;
        try {
            fw = new FileWriter(mFile);
            fw.write("%s:%s".formatted(name, password) + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }
}
