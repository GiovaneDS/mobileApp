package com.controlefinanceiro.easy.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.controlefinanceiro.easy.R;
import com.controlefinanceiro.easy.config.ConfiguracaoFirebase;
import com.controlefinanceiro.easy.helper.Base64Custon;
import com.controlefinanceiro.easy.helper.DateCustom;
import com.controlefinanceiro.easy.model.Movimentacao;
import com.controlefinanceiro.easy.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LancamentoFuturoActivity extends AppCompatActivity {
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Spinner spinerLancamento;
    private TextInputEditText campoDataInicial, campoQtdParcelas, campoDataFinal, campoDescricao, campoCategoria;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private Double receitaTotal ;
    private Double despesasTotal ;
    private String data;
    private Integer mesInicio;
    private Integer anoInicio;
    private String dataInicio;
    private Integer qtdParcelas = 0;
    private String dataLancamento;
    private String tipoLacamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancamento_futuro);

        campoCategoria = findViewById(R.id.txtCategoria);
        campoDataInicial = findViewById(R.id.txtDataInicial);
        campoQtdParcelas = findViewById(R.id.txtQuantidadeParcelas);
        campoDataFinal = findViewById(R.id.txtDataFinal);
        campoDescricao = findViewById(R.id.txtDescricao);
        campoValor = findViewById(R.id.txtValor);

        campoDataInicial.setText(DateCustom.dataAtual());
        campoDataFinal.setText(DateCustom.dataAtual());
        campoQtdParcelas.setText(String.valueOf(qtdParcelas));
        data = campoDataFinal.getText().toString();
        dataInicio = campoDataInicial.getText().toString();

        spinerLancamento = findViewById(R.id.spinnerLancamento);
        List<String> lancamento = new ArrayList<>(Arrays.asList("Receita", "Despesa"));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lancamento);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinerLancamento.setAdapter(dataAdapter);

        spinerLancamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //int tipo = parent.getSelectedItemPosition();
                final String vnome = parent.getSelectedItem().toString();
                recuperaTipoSelecionado(vnome);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        campoQtdParcelas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

               if (campoQtdParcelas != null || !campoQtdParcelas.equals("")) {
                    try {
                        calcularDataFinal();
                        Toast.makeText(LancamentoFuturoActivity.this,
                                "Data alterada",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(LancamentoFuturoActivity.this,
                                "Quantidade de parcelas nao pode ser vazio",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        recuperaDespesasTotal();
        recuperaReceitasTotal();
    }

    public String recuperaTipoSelecionado(String tipo){

        if(tipo == "Receita"){
            tipoLacamento = "r";
        } else if(tipo == "Despesa"){
            tipoLacamento = "d";
        }
        Log.i("tipo", "tipo selecionado " + tipoLacamento);
         return tipoLacamento;
    }

    public void calcularDataFinal() {

       String dataFinal;
        qtdParcelas = Integer.parseInt(campoQtdParcelas.getText().toString());

        String retornoData[] = data.split("/");
        String dia = retornoData[0];//dia 23
        String mes = retornoData[1];//mes 01
        String ano = retornoData[2];//ano 2018

        mesInicio = Integer.parseInt(mes);
        anoInicio = Integer.parseInt(ano);

        for (int i = 0; i < qtdParcelas; i++ ) {
            if (mesInicio > 12){
                mesInicio = 01;
                anoInicio++;
                dataFinal = dia + "/" + DateCustom.mesDoisDigitos(String.valueOf(mesInicio)) + "/" + anoInicio;
                Log.i("msg", "Mes final calculado " + dataFinal);
                mesInicio++;
            }else{
                dataFinal = dia + "/" + DateCustom.mesDoisDigitos(String.valueOf(mesInicio)) + "/" + anoInicio;
                Log.i("msg", "Mes final calculado " + dataFinal);
                mesInicio++;
            }

            campoDataFinal.setText(dataFinal);
               /*Toast.makeText(LancamentoFuturoActivity.this,
                "Ate aqui esta chegando " + dataFinal,
                Toast.LENGTH_SHORT).show();*/
        }
    }

    public void salvarLancamento(View view) {

        if (validarCamposLancamento()) {
            executaSalvarLancamento();
        }
    }

    public void executaSalvarLancamento() {

        movimentacao = new Movimentacao();
        qtdParcelas = Integer.parseInt(campoQtdParcelas.getText().toString());

        String retornoData[] = dataInicio.split("/");
        String dia = retornoData[0];//dia 23
        String mes = retornoData[1];//mes 01
        String ano = retornoData[2];//ano 2018

        mesInicio = Integer.parseInt(mes);
        anoInicio = Integer.parseInt(ano);

        Log.i("msg", "Mes inicial " + mesInicio);
        Log.i("msg", "Quantidade de Parcelas " + qtdParcelas);

        for (int i = 0; i < qtdParcelas; i++ ) {

           if (mesInicio > 12){
               mesInicio = 01;
               anoInicio++;
               dataLancamento = dia + "/" + DateCustom.mesDoisDigitos(String.valueOf(mesInicio)) + "/" + anoInicio;
               Log.i("msg", "Mes final calculado " + dataLancamento);
               mesInicio++;
           }else{
               dataLancamento = dia + "/" + DateCustom.mesDoisDigitos(String.valueOf(mesInicio)) + "/" + anoInicio;
               Log.i("msg", "Mes final calculado " + dataLancamento);
               mesInicio++;
           }

            Double valorRecuperado = Double.parseDouble(campoValor.getText().toString());
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(campoCategoria.getText().toString());
            movimentacao.setDescricao(campoDescricao.getText().toString());
            movimentacao.setData(dataLancamento);


            movimentacao.setTipo(recuperaTipoSelecionado(tipoLacamento));

            String pegaDataAtual = DateCustom.mesAnoDataEscolhida(DateCustom.dataAtual());
            int dataAtual = Integer.parseInt(pegaDataAtual);
            String pegadataDoisDigitos = DateCustom.mesAnoDataEscolhida(movimentacao.getData());
            int pegaData = Integer.parseInt(pegadataDoisDigitos);

            if(recuperaTipoSelecionado(tipoLacamento).equals("r") && pegaData <= dataAtual){
                Double totalReceitas = receitaTotal + valorRecuperado;
                movimentacao.setFlag("debitado");
                atualizarReceita( totalReceitas );
            }
            if (recuperaTipoSelecionado(tipoLacamento).equals("d") && pegaData <= dataAtual){
                Double totalDespesas = despesasTotal + valorRecuperado;
                movimentacao.setFlag("debitado");
                atualizarDespesa( totalDespesas );

            }

            if(recuperaTipoSelecionado(tipoLacamento).equals("d") && pegaData > dataAtual) {
                movimentacao.setFlag("noDebitado");
            }

            if(recuperaTipoSelecionado(tipoLacamento).equals("r") && pegaData > dataAtual) {
                movimentacao.setFlag("noDebitado");
            }
            movimentacao.salvar(dataLancamento);
        }
    }

    public void recuperaDespesasTotal(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class);
                despesasTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void recuperaReceitasTotal(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class);
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void atualizarDespesa( Double despesa){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.child("despesaTotal").setValue(despesa);
    }

    public void atualizarReceita( Double receita){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.child("receitaTotal").setValue(receita);
    }

    public Boolean validarCamposLancamento(){

        String textValor = campoValor.getText().toString();
        String textDataInicial = campoDataInicial.getText().toString();
        String textDataFinal = campoDataFinal.getText().toString();
        String textCategoria = campoCategoria.getText().toString();
        String textDescricao = campoDescricao.getText().toString();
        String textQtdParcelas = campoQtdParcelas.getText().toString();

        if (!textValor.isEmpty()){
            if (!textDataInicial.isEmpty()){
                if (!textCategoria.isEmpty()){
                     if (!textDescricao.isEmpty()){
                         if (!textDataFinal.isEmpty()){
                             if (!textQtdParcelas.isEmpty()){
                                 return true;
                             }else{
                                 Toast.makeText(LancamentoFuturoActivity.this,
                                         "A quantidade de parcelas nao foi preenchida!",
                                         Toast.LENGTH_SHORT).show();
                                 return false;
                             }

                         }else {
                             Toast.makeText(LancamentoFuturoActivity.this,
                                     "A data Final nao foi preenchida!",
                                     Toast.LENGTH_SHORT).show();
                             return false;
                         }

                        }else {
                         Toast.makeText(LancamentoFuturoActivity.this,
                                  "A descrição nao foi preenchida!",
                                   Toast.LENGTH_SHORT).show();
                           return false;
                    }

                }else {
                    Toast.makeText(LancamentoFuturoActivity.this,
                            "A categoria não foi preenchida!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

            }else {
                Toast.makeText(LancamentoFuturoActivity.this,
                        "Preencha a data!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(LancamentoFuturoActivity.this,
                    "Preencha o valor da despesa!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

    }

}