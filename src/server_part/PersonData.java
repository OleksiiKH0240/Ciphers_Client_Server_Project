package server_part;

import java.io.*;

/**
 * class to saving\loading client`s data(name, password)
 */
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
     * give person`s password by person`s name
     * @param name unique name of person
     * @return
     * <p>password if all is ok;</p>
     * <p>empty String if Person with such name was not found;</p>
     * <p>'FileNotExist' if file of person`s strings does not exist.</p>
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
     * save person to the file /resource/PersonsFile.txt or to the file specified in constructor
     * @param name unique name of person
     * @param password password of person, that used to log in
     * @return
     * <p>0 if all is ok;</p>
     * <p>1 if exist person in file with same name;</p>
     * <p>-1 if file does not exist.</p>
     * <p>Person fill one row in the file 'PersonsFile.txt', which looks like 'PersonName:PersonPassword' .</p>
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
