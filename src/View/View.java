package View;

import javax.swing.JOptionPane;


public class View {
    public static void render(String message){
        render(message, "");
    }
    public static void render(String message, String typeMessage){
        int numberType;
        switch(typeMessage){
            case "error":
                numberType = JOptionPane.ERROR_MESSAGE;
                break;
            case "question":
                numberType = JOptionPane.QUESTION_MESSAGE;
                break;
            default:
                numberType = JOptionPane.INFORMATION_MESSAGE;
                break;
        }
        JOptionPane.showMessageDialog(null, message, "MENSAGEM DO SISTEMA:",numberType);
    }
}
