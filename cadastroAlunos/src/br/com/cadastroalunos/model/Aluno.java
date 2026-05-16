package br.com.cadastroalunos.model;

/**
 * Classe modelo que representa um Aluno no sistema de cadastro acadêmico.
 *
 * <p>
 * No padrão MVC (Model-View-Controller), esta classe é o "Model" — ela apenas
 * guarda os dados do aluno, sem se preocupar com tela ou banco de dados.
 * </p>
 *
 * <p>
 * Ela contém dois tipos de atributos:
 * </p>
 * <ul>
 * <li><b>Dados próprios do aluno:</b> id, rgm, nome, cpf, etc.</li>
 * <li><b>Dados vindos do curso (via JOIN):</b> nomeCurso, campus, periodo.</li>
 * </ul>
 *
 * @author Equipe ADS
 * @version 1.0
 */
public class Aluno {

	// =========================================================
	// ATRIBUTOS — cada um armazena uma informação do aluno
	// =========================================================

	/**
	 * Identificador único gerado automaticamente pelo banco de dados (chave
	 * primária).
	 */
	private int id;

	/**
	 * Registro Geral do aluno na instituição — equivalente ao "número de
	 * matrícula".
	 */
	private String rgm;

	/** Nome completo do aluno. */
	private String nome;

	/**
	 * Data de nascimento do aluno. Armazenada como texto para facilitar exibição na
	 * tela. Formato esperado: dd/MM/yyyy (ex: 15/08/2001)
	 */
	private String dataNascimento;

	/**
	 * CPF do aluno — documento de identificação fiscal. Formato esperado:
	 * 000.000.000-00 (ex: 123.456.789-09)
	 */
	private String cpf;

	/** Endereço de e-mail do aluno para contato e notificações. */
	private String email;

	/** Logradouro completo do aluno (rua, número, complemento). */
	private String endereco;

	/** Cidade onde o aluno reside. */
	private String municipio;

	/**
	 * Sigla do estado (Unidade Federativa) onde o aluno reside. Exemplo: "SP",
	 * "RJ", "MG"
	 */
	private String uf;

	/**
	 * Número de celular do aluno para contato. Formato esperado: (00) 00000-0000
	 * (ex: (11) 98765-4321)
	 */
	private String celular;

	/**
	 * Chave estrangeira que referencia o curso em que o aluno está matriculado.
	 * Este número corresponde ao "id" da tabela Curso no banco de dados.
	 */
	private int cursoId;

	// =========================================================
	// ATRIBUTOS AUXILIARES — preenchidos via JOIN com a tabela Curso
	// Estes campos NÃO existem na tabela "aluno" do banco,
	// mas são trazidos junto na consulta para evitar buscas extras.
	// =========================================================

	/** Nome do curso do aluno, obtido via JOIN com a tabela Curso. */
	private String nomeCurso;

	/** Campus onde o curso é oferecido, obtido via JOIN com a tabela Curso. */
	private String campus;

	/**
	 * Período/turno do curso (ex: "Manhã", "Noite"), obtido via JOIN com a tabela
	 * Curso.
	 */
	private String periodo;

	// =========================================================
	// CONSTRUTORES — formas de criar um objeto Aluno
	// =========================================================

	/**
	 * Construtor padrão (vazio).
	 *
	 * Necessário para que frameworks como Hibernate ou JavaFX consigam criar um
	 * objeto Aluno sem precisar passar dados. Depois de criado, os dados são
	 * setados um a um via setters.
	 */
	public Aluno() {
	}

	/**
	 * Construtor completo — cria um Aluno já com todos os dados preenchidos.
	 *
	 * <p>
	 * Note que o {@code id} não é passado aqui porque ele é gerado automaticamente
	 * pelo banco de dados (AUTO_INCREMENT).
	 * </p>
	 *
	 * @param rgm            Registro Geral do aluno na instituição
	 * @param nome           Nome completo do aluno
	 * @param dataNascimento Data de nascimento no formato dd/MM/yyyy
	 * @param cpf            CPF no formato 000.000.000-00
	 * @param email          E-mail do aluno
	 * @param endereco       Endereço completo (rua, número, complemento)
	 * @param municipio      Cidade de residência
	 * @param uf             Sigla do estado (ex: "SP")
	 * @param celular        Celular no formato (00) 00000-0000
	 * @param cursoId        ID do curso em que o aluno está matriculado
	 */
	public Aluno(String rgm, String nome, String dataNascimento, String cpf, String email, String endereco,
			String municipio, String uf, String celular, int cursoId) {
		this.rgm = rgm;
		this.nome = nome;
		this.dataNascimento = dataNascimento;
		this.cpf = cpf;
		this.email = email;
		this.endereco = endereco;
		this.municipio = municipio;
		this.uf = uf;
		this.celular = celular;
		this.cursoId = cursoId;
	}

	// =========================================================
	// GETTERS E SETTERS
	// Como os atributos são "private", a única forma de acessá-los
	// de fora da classe é através desses métodos públicos.
	// Getter = lê o valor | Setter = altera o valor
	// =========================================================

	/**
	 * Retorna o ID gerado pelo banco de dados.
	 * 
	 * @return id do aluno
	 */
	public int getId() {
		return id;
	}

	/**
	 * Define o ID do aluno — normalmente chamado pelo DAO após inserção no banco.
	 * 
	 * @param id identificador gerado pelo banco
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retorna o RGM (número de matrícula) do aluno.
	 * 
	 * @return rgm do aluno
	 */
	public String getRgm() {
		return rgm;
	}

	/**
	 * Define o RGM do aluno.
	 * 
	 * @param rgm número de matrícula
	 */
	public void setRgm(String rgm) {
		this.rgm = rgm;
	}

	/**
	 * Retorna o nome completo do aluno.
	 * 
	 * @return nome do aluno
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Define o nome completo do aluno.
	 * 
	 * @param nome nome do aluno
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Retorna a data de nascimento no formato dd/MM/yyyy.
	 * 
	 * @return data de nascimento
	 */
	public String getDataNascimento() {
		return dataNascimento;
	}

	/**
	 * Define a data de nascimento do aluno.
	 * 
	 * @param dataNascimento data no formato dd/MM/yyyy
	 */
	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	/**
	 * Retorna o CPF do aluno no formato 000.000.000-00.
	 * 
	 * @return cpf do aluno
	 */
	public String getCpf() {
		return cpf;
	}

	/**
	 * Define o CPF do aluno.
	 * 
	 * @param cpf CPF no formato 000.000.000-00
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	/**
	 * Retorna o e-mail do aluno.
	 * 
	 * @return email do aluno
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Define o e-mail do aluno.
	 * 
	 * @param email endereço de e-mail
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Retorna o endereço completo do aluno.
	 * 
	 * @return endereço do aluno
	 */
	public String getEndereco() {
		return endereco;
	}

	/**
	 * Define o endereço do aluno.
	 * 
	 * @param endereco logradouro completo
	 */
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	/**
	 * Retorna o município (cidade) onde o aluno reside.
	 * 
	 * @return município do aluno
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * Define o município de residência do aluno.
	 * 
	 * @param municipio nome da cidade
	 */
	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	/**
	 * Retorna a sigla do estado (UF) onde o aluno reside.
	 * 
	 * @return UF do aluno (ex: "SP")
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * Define a UF do aluno.
	 * 
	 * @param uf sigla do estado (ex: "SP", "RJ")
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}

	/**
	 * Retorna o número de celular do aluno no formato (00) 00000-0000.
	 * 
	 * @return celular do aluno
	 */
	public String getCelular() {
		return celular;
	}

	/**
	 * Define o celular do aluno.
	 * 
	 * @param celular número no formato (00) 00000-0000
	 */
	public void setCelular(String celular) {
		this.celular = celular;
	}

	/**
	 * Retorna o ID do curso em que o aluno está matriculado.
	 * 
	 * @return cursoId (chave estrangeira referenciando a tabela Curso)
	 */
	public int getCursoId() {
		return cursoId;
	}

	/**
	 * Define o ID do curso do aluno.
	 * 
	 * @param cursoId ID do curso na tabela Curso
	 */
	public void setCursoId(int cursoId) {
		this.cursoId = cursoId;
	}

	/**
	 * Retorna o nome do curso do aluno.
	 *
	 * <p>
	 * Usa um operador ternário para evitar retornar {@code null}: se
	 * {@code nomeCurso} for nulo (ex: JOIN não foi feito), retorna uma string vazia
	 * em vez de causar um NullPointerException.
	 * </p>
	 *
	 * @return nome do curso, ou "" se não estiver preenchido
	 */
	public String getNomeCurso() {
		return nomeCurso != null ? nomeCurso : "";
	}

	/**
	 * Define o nome do curso — chamado ao montar o objeto com resultado do JOIN.
	 * 
	 * @param nomeCurso nome do curso vindo da tabela Curso
	 */
	public void setNomeCurso(String nomeCurso) {
		this.nomeCurso = nomeCurso;
	}

	/**
	 * Retorna o campus onde o curso do aluno é oferecido.
	 * 
	 * @return campus do curso
	 */
	public String getCampus() {
		return campus;
	}

	/**
	 * Define o campus — chamado ao montar o objeto com resultado do JOIN.
	 * 
	 * @param campus campus vindo da tabela Curso
	 */
	public void setCampus(String campus) {
		this.campus = campus;
	}

	/**
	 * Retorna o período/turno do curso do aluno (ex: "Manhã", "Noite").
	 * 
	 * @return período do curso
	 */
	public String getPeriodo() {
		return periodo;
	}

	/**
	 * Define o período — chamado ao montar o objeto com resultado do JOIN.
	 * 
	 * @param periodo turno do curso vindo da tabela Curso
	 */
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	// =========================================================
	// MÉTODOS SOBRESCRITOS
	// =========================================================

	/**
	 * Retorna uma representação textual do aluno.
	 *
	 * <p>
	 * Sobrescreve o {@code toString()} padrão da classe Object para exibir
	 * informações úteis. Esse formato é muito prático em ComboBoxes e listas de
	 * seleção da interface gráfica, onde o Java chama toString() automaticamente
	 * para exibir o item.
	 * </p>
	 *
	 * <p>
	 * Exemplo de retorno: {@code "2023001 - João da Silva"}
	 * </p>
	 *
	 * @return string no formato "rgm - nome"
	 */
	@Override
	public String toString() {
		return rgm + " - " + nome;
	}
}