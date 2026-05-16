package br.com.cadastroalunos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitária responsável por gerenciar a conexão com o banco de dados
 * MySQL.
 *
 * 
 * Fica no pacote util porque não representa um dado do sistema (como
 * Aluno ou Curso), mas sim uma ferramenta de infraestrutura usada pelas classes
 * DAO para se comunicar com o banco.
 * 
 *
 * <p>
 * Todos os métodos são {@code static}, ou seja, podem ser chamados diretamente
 * pela classe sem precisar criar um objeto {@code new ConexaoDB()}:
 * </p>
 * 
 * <pre>
 * Connection conn = ConexaoDB.getConexao();
 * ConexaoDB.fecharConexao(conn);
 * </pre>
 *
 * <p>
 * <b>Atenção:</b> usuário e senha estão escritos diretamente no código
 * (hardcoded). Em produção, o ideal seria ler essas informações de um arquivo
 * de configuração externo (como um {@code .properties}), para não expor
 * credenciais no repositório.
 * </p>
 *
 * @author Equipe ADS
 * @version 1.0
 */
public class ConexaoDB {

	/**
	 * Abre e retorna uma conexão com o banco de dados MySQL.
	 *
	 * <p>
	 * O processo interno ocorre em três etapas:
	 * </p>
	 * <ol>
	 * <li><b>Carrega o Driver JDBC</b> — o {@code Class.forName()} localiza e
	 * registra o driver do MySQL na JVM. Sem isso, o Java não sabe como se
	 * comunicar com o banco.</li>
	 * <li><b>Monta a URL de conexão</b> — contém o endereço do servidor, porta,
	 * nome do banco e parâmetros extras (SSL desativado e fuso horário).</li>
	 * <li><b>Abre a conexão</b> — o {@code DriverManager.getConnection()} usa a
	 * URL, usuário e senha para estabelecer a conexão e a retorna.</li>
	 * </ol>
	 *
	 * <p>
	 * Anatomia da URL JDBC:
	 * </p>
	 * 
	 * <pre>
	 *   jdbc:mysql://localhost:3306/sistema_academico?useSSL=false&serverTimezone=America/Sao_Paulo
	 *   │           │         │    │                  │            │
	 *   protocolo   host    porta  banco de dados      sem SSL     fuso horário
	 * </pre>
	 *
	 * @return um objeto {@code Connection} pronto para executar queries no banco
	 * @throws SQLException se o driver não for encontrado ou a conexão falhar
	 *                      (banco offline, credenciais erradas, etc.)
	 */
	public static Connection getConexao() throws SQLException {
		try {
			// Registra o driver JDBC do MySQL na JVM.
			// Sem isso, o DriverManager não sabe como falar com o MySQL.
			Class.forName("com.mysql.cj.jdbc.Driver");

			// URL de conexão com o banco:
			// - useSSL=false: desativa SSL (evita warnings em ambiente local)
			// - serverTimezone: define o fuso para evitar erros com datas/horas
			String url = "jdbc:mysql://localhost:3306/sistema_academico?useSSL=false&serverTimezone=America/Sao_Paulo";

			String usuario = "root";
			String senha = "";

			// Abre a conexão e a retorna para quem chamou o método
			return DriverManager.getConnection(url, usuario, senha);

		} catch (ClassNotFoundException e) {
			// Converte ClassNotFoundException em SQLException para simplificar
			// o tratamento de erros nas classes DAO (que só precisam capturar SQLException)
			throw new SQLException("Driver JDBC não encontrado: verifique se o conector MySQL está no classpath.");
		}
	}

	/**
	 * Fecha a conexão com o banco de dados com segurança.
	 *
	 * <p>
	 * Sempre verifique se {@code conn != null} antes de fechar — isso evita um
	 * {@code NullPointerException} caso a conexão nunca tenha sido aberta com
	 * sucesso.
	 * </p>
	 *
	 * <p>
	 * Este método deve ser chamado no bloco {@code finally} das classes DAO,
	 * garantindo que a conexão seja fechada mesmo se uma exceção ocorrer:
	 * </p>
	 * 
	 * <pre>
	 * Connection conn = null;
	 * try {
	 * 	conn = ConexaoDB.getConexao();
	 * 	// executa queries...
	 * } finally {
	 * 	ConexaoDB.fecharConexao(conn);
	 * }
	 * </pre>
	 *
	 * @param conn a conexão a ser fechada (pode ser {@code null} sem causar erro)
	 * @throws SQLException se ocorrer um erro ao tentar fechar a conexão
	 */
	public static void fecharConexao(Connection conn) throws SQLException {
		try {
			// Só tenta fechar se a conexão realmente foi aberta
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			// Relança com mensagem mais descritiva para facilitar o diagnóstico
			throw new SQLException("Erro ao fechar conexão: " + e.getMessage());
		}
	}
}