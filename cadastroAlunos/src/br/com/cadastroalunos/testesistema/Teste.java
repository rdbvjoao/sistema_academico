package br.com.cadastroalunos.testesistema;

import java.sql.Connection;

import br.com.cadastroalunos.util.ConexaoDB;


public class Teste {

    public static void main(String[] args) 
    {

    	try {

            Connection conn = ConexaoDB.getConexao();

            if (conn != null) 
            {
                System.out.println("Conectado ao BD com sucesso!");
            }

        } catch (Exception e) {

            System.out.println("Erro ao conectar: ");
            e.printStackTrace();
        }}
    	
    }
