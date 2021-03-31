package sci.changeFiscalInputDate;

import java.io.File;
import javax.swing.JOptionPane;
import java.util.Calendar;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import sql.Database;

public class SciChangeFiscalInputDate {

    private static int empresa;
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
            View.render("Empresa inválida!");
        }
    }

    private static boolean pegarArquivo() {
        View.render("Escolha o arquivo csv com a chave do lançamento de entrada na primeira coluna e a nova data na segunda coluna:");
        try {
            arquivo = Selector.Arquivo.selecionar("C:\\", "CSV", "csv");
            return Selector.Arquivo.verifica(arquivo.getAbsolutePath(), "csv");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean pegarEmpresa() {
        String empresaStr = JOptionPane.showInputDialog("Digite o número da empresa na SCI:");
        try {
            empresa = Integer.valueOf(empresaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void alterarDatas() {
        //Conecta banco
        Banco banco = new Banco("sci.cfg");

        if (banco.testConnection()) {
            String textoArquivo = Arquivo.ler(arquivo.getAbsolutePath());
            String[] linhasArquivo = textoArquivo.split("\r\n");
            int max = linhasArquivo.length;

            //comeca carregamento
            JFrame progresso = new Carregamento();
            progresso.setVisible(true);
            Carregamento.barra.setMinimum(0);
            Carregamento.barra.setMaximum(max);

            //percorre linhas
            for (int i = 0; i < max; i++) {

                //atualiza barra
                Carregamento.barra.setValue(i);
                Carregamento.texto.setText(i + " de " + max);

                String linha = linhasArquivo[i];
                String[] colunas = linha.split(";");

                int chaveEntrada;
                Calendar data = Calendar.getInstance();

                try {
                    //Verifica se primeira coluna é numero
                    chaveEntrada = Integer.valueOf(colunas[0]);
                    //verifica se primeira coluna é data valida
                    data = getDateOfString(colunas[1]);
                    if (data != null) {
                        String dataSql = getDateSqlOfCalendar(data);
                        String sql = "UPDATE VEF_EMP_TMOVENT e\n"
                                + "set e.BDDATAENTRADAENT = '" + dataSql + "'\n"
                                + "where \n"
                                + "e.BDCODEMP = '" + empresa + "' and\n"
                                + "e.BDCHAVE  = '" + chaveEntrada + "' ;";
                        //executa sql
                        banco.query(sql);
                    }
                } catch (Exception e) {
                    System.out.println("Erro: " + e);
                }
            }
            
            View.render("Programa terminado! Confira as modificações na SCI.");
        } else {
            View.render("Erro ao conectar no banco de dados!");
        }

        System.exit(0);
    }

    private static String getDateSqlOfCalendar(Calendar date) {
        return date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH);
    }

    private static Calendar getDateOfString(String dataStr) {
        String regex = "^[0-3]?[0-9]\\/[0-1]?[0-9]\\/(?:[0-9]{2})[0-9]{2}$";

        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(dataStr).matches()) {
            Calendar date = Calendar.getInstance();
            try {
                String[] dataSplit = dataStr.split("/");
                date.set(Integer.valueOf(dataSplit[2]), Integer.valueOf(dataSplit[1]) - 1, Integer.valueOf(dataSplit[0]));
                return date;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
