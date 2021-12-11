package server_part;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class to saving/loading client`s messages
 */
public class MessageData {
    private String mFilePath;
    private File mFile;

    public MessageData() {
        String basePath = new File("").getAbsolutePath();
        mFile = new File(basePath + "/resource/MessagesFile.txt");
    }

    public MessageData(String filePath) {
        mFilePath = filePath;
        mFile = new File(mFilePath);
    }

    /**
     * save client`s message in /resource/MessagesFile.txt file or in file specified in constructor
     * @param name           - identifier for data in 'MessagesFile.txt'
     * @param encryptingType - encrypting type of message, that server was given
     * @param message        - message, that server was given
     * @return - status codes = {-2, -1, 0}
     * 0 - all is ok (message was saved to the file 'MessagesFile.txt');
     * -1 - file was not found;
     * -2 - exception during input/output to the file 'MessagesFile.txt'.
     */
    public int saveMessage(String name, String encryptingType, String message) {
        FileReader fr;
        try {
            fr = new FileReader(mFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
        String fileContent = "";
        String newFileContent = "";
        int i;
        try {
            while ((i = fr.read()) != -1) {
                fileContent += ((char) i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -2;
        }

//      regular expression  for searching elements with message in file 'MessagesFile.txt'
        String regexp = "<tok>\\w+</tok> <tok>[\\w/]+</tok> <tok>(.\\s{0,2})+</tok>";
        Pattern replaceTok = Pattern.compile(regexp);
        Matcher matcher = replaceTok.matcher(fileContent);

        String[] tokens;
        String group;
        String currName;
        String newLine = "";
        while (matcher.find()) {
            int startIdx = matcher.start();
            int endIdx = matcher.end();

//            element with message
            group = fileContent.substring(startIdx, endIdx);

//            splitting element to tokens = ["<tok>name", "encryptingType", "message</tok>"]
            tokens = group.split("</tok> <tok>");
            tokens[0] = tokens[0].replace("<tok>", "");
//            tokens[2] = tokens[2].replace("</tok>", "");
            currName = tokens[0];

            if (currName.equals(name)) {
                newLine = "<tok>%s</tok> <tok>%s</tok> <tok>%s</tok>".formatted(currName, encryptingType, message);

//                adding new modified line to the string 'newFileContent',
//                which will rewrote file 'MessagesFile.txt' in future
                newFileContent += newLine;
                String restContent = fileContent.substring(endIdx, fileContent.length());
                newFileContent += restContent;
                break;
            } else {
                newFileContent += (group + "\n\n\n");
            }
        }
        FileWriter fw;
        if (!newLine.equals("")) {
            try {
                fr.close();
                fw = new FileWriter(mFile);
                fw.write(newFileContent);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -2;
            }
            return 0;
        } else {
            try {
                fr.close();
                fw = new FileWriter(mFile);
                newLine = "<tok>%s</tok> <tok>%s</tok> <tok>%s</tok>".formatted(name, encryptingType, message);
//                adding new modified line to the string 'newFileContent' with all other file content in the begin ,
//                to rewrite file 'MessagesFile.txt'
                newFileContent = fileContent + newLine + "\n\n\n";
                fw.write(newFileContent);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -2;
            }
        }

        return 0;
    }

    /**
     * load client`s message from /resource/MessagesFile.txt file or from file specified in constructor
     * @param name identifier of client`s encrypted message
     * @return
     * client`s encrypted message with next form "<tok>encryptingType</tok> <tok>message</tok>", if all is ok
     * "Message, with given name, was not found", if there are no messages in file with name identifier
     * "FileNotFound", if file /resource/MessagesFile.txt or file that specified in constructor was not found
     * "InputOutputException", if there are some problems with reading file
     */
    public String loadMessage(String name) {
        FileReader fr;
        try {
            fr = new FileReader(mFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FileNotFound";
        }

        String fileContent = "";
        int i;
        try {
            while ((i = fr.read()) != -1) {
                fileContent += ((char) i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "InputOutputException";
        }

        String response = "";
        String regexp = "<tok>\\w+</tok> <tok>[\\w/]+</tok> <tok>(.\\s{0,2})+</tok>";
        Pattern replaceTok = Pattern.compile(regexp);
        Matcher matcher = replaceTok.matcher(fileContent);

        String[] tokens;
        String group;
        String currName;
        String encryptingType;
        String message;
        while (matcher.find()) {
            int startIdx = matcher.start();
            int endIdx = matcher.end();

//            element with message
            group = fileContent.substring(startIdx, endIdx);

//            splitting element to tokens = ["<tok>name", "encryptingType", "message</tok>"]
            tokens = group.split("</tok> <tok>");
            tokens[0] = tokens[0].replace("<tok>", "");
            tokens[2] = tokens[2].replace("</tok>", "");
            currName = tokens[0];
            encryptingType = tokens[1];
            message = tokens[2];

            if (currName.equals(name)) {
//              found needed element with provided 'name'
//              and return string, which looks like '<tok>encryptingType</tok> <tok>message</tok>'
                response = "<tok>%s</tok> <tok>%s</tok>".formatted(encryptingType, message);
                break;
            }
        }

        return (response.equals("")) ? "Message, with given name, was not found" : response;
    }
}