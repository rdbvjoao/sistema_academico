package br.com.cadastroalunos.model;

/**
 * Classe modelo que representa um Curso no sistema acadêmico.
 *
 * <p>
 * Assim como a classe Aluno, esta segue o padrão MVC como "Model" — sua única
 * responsabilidade é guardar os dados de um curso.
 * </p>
 *
 * <p>
 * Os atributos desta classe também aparecem na classe {@code Aluno} como campos
 * auxiliares (nomeCurso, campus, periodo), preenchidos via JOIN quando se busca
 * um aluno com os dados do seu curso.
 * </p>
 *
 * @author Equipe ADS
 * @version 1.0
 */
public class Curso {

	// =========================================================
	// ATRIBUTOS — cada um armazena uma informação do curso
	// =========================================================

	/**
	 * Identificador único gerado automaticamente pelo banco de dados (chave
	 * primária).
	 */
	private int id;

	/** Nome do curso (ex: "Análise e Desenvolvimento de Sistemas"). */
	private String nome;

	/** Campus onde o curso é oferecido (ex: "Unidade Centro"). */
	private String campus;

	/** Período/turno em que o curso é ministrado (ex: "Manhã", "Noite"). */
	private String periodo;

	// =========================================================
	// CONSTRUTORES
	// =========================================================

	/**
	 * Construtor padrão (vazio).
	 *
	 * Necessário para que frameworks e a própria aplicação consigam criar um objeto
	 * Curso vazio e preencher os dados depois via setters. Muito usado ao popular
	 * ComboBoxes e listas na interface gráfica.
	 */
	public Curso() {
	}

	/**
	 * Construtor completo — cria um Curso já com todos os dados preenchidos.
	 *
	 * <p>
	 * Diferente da classe Aluno, aqui o {@code id} é recebido como parâmetro. Isso
	 * é útil quando o curso já existe no banco e precisamos montar o objeto com os
	 * dados retornados pela consulta (SELECT).
	 * </p>
	 *
	 * @param id      Identificador do curso vindo do banco de dados
	 * @param nome    Nome do curso
	 * @param campus  Campus onde o curso é oferecido
	 * @param periodo Período/turno do curso
	 */
	public Curso(int id, String nome, String campus, String periodo) {
		this.id = id;
		this.nome = nome;
		this.campus = campus;
		this.periodo = periodo;
	}

	// =========================================================
	// GETTERS E SETTERS
	// Como os atributos são "private", a única forma de acessá-los
	// de fora da classe é através desses métodos públicos.
	// Getter = lê o valor | Setter = altera o valor
	// =========================================================

	/**
	 * Retorna o ID do curso gerado pelo banco de dados.
	 * 
	 * @return id do curso
	 */
	public int getId() {
		return id;
	}

	/**
	 * Define o ID do curso — normalmente chamado pelo DAO após consulta no banco.
	 * 
	 * @param id identificador gerado pelo banco
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retorna o nome do curso.
	 * 
	 * @return nome do curso (ex: "Análise e Desenvolvimento de Sistemas")
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Define o nome do curso.
	 * 
	 * @param nome nome do curso
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Retorna o campus onde o curso é oferecido.
	 * 
	 * @return campus do curso
	 */
	public String getCampus() {
		return campus;
	}

	/**
	 * Define o campus do curso.
	 * 
	 * @param campus nome ou identificação do campus
	 */
	public void setCampus(String campus) {
		this.campus = campus;
	}

	/**
	 * Retorna o período/turno do curso.
	 * 
	 * @return período do curso (ex: "Manhã", "Noite")
	 */
	public String getPeriodo() {
		return periodo;
	}

	/**
	 * Define o período/turno do curso.
	 * 
	 * @param periodo turno em que o curso é ministrado
	 */
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	// =========================================================
	// MÉTODOS SOBRESCRITOS
	// =========================================================

	/**
	 * Retorna uma representação textual completa do curso.
	 *
	 * <p>
	 * Sobrescreve o {@code toString()} padrão da classe Object para combinar os
	 * três campos mais relevantes em uma única string legível. Muito útil em
	 * ComboBoxes da interface gráfica, onde o Java chama esse método
	 * automaticamente para exibir cada item da lista.
	 * </p>
	 *
	 * <p>
	 * O retorno é quebrado em múltiplas linhas apenas para melhorar a leitura do
	 * código — o resultado final é uma única string contínua.
	 * </p>
	 *
	 * <p>
	 * Exemplo de retorno:
	 * {@code "Análise e Desenvolvimento de Sistemas - Unidade Centro (Noite)"}
	 * </p>
	 *
	 * @return string no formato "nome - campus (periodo)"
	 */
	@Override
	public String toString() {
		return nome + " - " + campus + " (" + periodo + ")";
	}
}