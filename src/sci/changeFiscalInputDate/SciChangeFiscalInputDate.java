package sci.changeFiscalInputDate;

import SimpleView.Loading;
import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import javax.swing.JOptionPane;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import sql.Database;

public class SciChangeFiscalInputDate {

    private static Integer enterprise;
    private static File arquivo;

    public static void main(String[] args) {
        //pega empresa
        if (pegarEmpresa()) {
            //pega arquivo
            if (pegarArquivo()) {
                //importa valores aquivo
                alterarDatas();
            }
            //Não precisa de else pois o proprio selector os informa os erros
        } else {
            JOptionPane.showMessageDialog(null, "Empresa inválida!", "Empresa Inválida!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean pegarArquivo() {
        JOptionPane.showMessageDialog(null, "Escolha o arquivo csv com a chave do lançamento de entrada na primeira coluna e a nova data na segunda coluna:", "Escolha o arquivo:", JOptionPane.QUESTION_MESSAGE);
        try {
            arquivo = Selector.selectFile("C:\\", "CSV", "csv");
            return Selector.verifyFile(arquivo.getAbsolutePath(), true, "csv");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean pegarEmpresa() {
        String empresaStr = JOptionPane.showInputDialog("Digite o número da empresa na SCI:");
        try {
            enterprise = Integer.valueOf(empresaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void alterarDatas() {
        //Conecta banco
        Database.setStaticObject(new Database("sci.cfg"));

        if (Database.getDatabase().testConnection()) {
            String textoArquivo = FileManager.getText(arquivo.getAbsolutePath());
            String[] lines = textoArquivo.split("\r\n");

            //comeca carregamento
            Loading loading = new Loading("Carregamento", -1, lines.length);
            String sql = "UPDATE VEF_EMP_TMOVENT e "
                    + "set e.BDDATAENTRADAENT = ':date' "
                    + "where "
                    + "e.BDCODEMP = ':enterprise' and "
                    + "e.BDCHAVE  = ':key' ;";
            Map<String,String> sqlSwaps = new HashMap<>();
            sqlSwaps.put("enterprise", enterprise.toString());

            for (String line : lines) {
                loading.next();

                String[] cols = line.split(";");

                try {
                    //Verifica se primeira coluna é numero
                    String key = cols[0];
                    //verifica se primeira coluna é data valida
                    String date = getDateOfString(cols[1]);
                    if (date != null) {
                        sqlSwaps.put("key", key);
                        sqlSwaps.put("date", date);
                        
                        //executa sql
                        Database.getDatabase().query(sql, sqlSwaps);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Erro: " + e);
                }
            }

            JOptionPane.showMessageDialog(null, "Programa terminado! Confira as modificações na SCI.");
        } else {
            JOptionPane.showMessageDialog(null, "Erro ao conectar no banco de dados!", "Erro!", JOptionPane.ERROR_MESSAGE);
        }

        System.exit(0);
    }

    private static String getDateOfString(String dataStr) {
        String regex = "^[0-3]?[0-9]\\/[0-1]?[0-9]\\/(?:[0-9]{2})[0-9]{2}$";

        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(dataStr).matches()) {
            try {
                String[] dataSplit = dataStr.split("/");
                String date = "";
                date += dataSplit[2] + "-";
                date += dataSplit[1] + "-";
                date += dataSplit[0];

                return date;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
