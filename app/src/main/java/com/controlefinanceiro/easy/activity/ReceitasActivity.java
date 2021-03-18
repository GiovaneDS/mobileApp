package com.controlefinanceiro.easy.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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



public class ReceitasActivity extends AppCompatActivity {
    private TextInputEditText campoData, campoDescricao, campoCategoria;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double receitaTotal;
    private Double receitaAtualizada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);
        campoCategoria = findViewById(R.id.txtCategoria);
        campoData = findViewById(R.id.txtData);
        campoDescricao = findViewById(R.id.txtDescricao);
        campoValor = findViewById(R.id.txtValor);
        campoData.setText(DateCustom.dataAtual());
        recuperaReceitaTotal();
    }

    public void salvarReceitas(View view){

        if (validarCamposReceitas()){
            movimentacao = new Movimentacao();
            String data = campoData.getText().toString();
            Double valorRecuperado = Double.parseDouble(campoValor.getText().toString());
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(campoCategoria.getText().toString());
            movimentacao.setDescricao(campoDescricao.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("r");
            receitaAtualizada = receitaTotal + valorRecuperado;
            atualizarReceita( receitaAtualizada );


            String pegaDataAtual = DateCustom.mesAnoDataEscolhida(DateCustom.dataAtual());
            int dataAtual = Integer.parseInt(pegaDataAtual);
            String pegadataDoisDigitos = DateCustom.mesAnoDataEscolhida(movimentacao.getData());
            int pegaData = Integer.parseInt(pegadataDoisDigitos);


            if(pegaData <= dataAtual){
                Double totalReceitas = receitaTotal + valorRecuperado;
                movimentacao.setFlag("debitado");
                atualizarReceita( totalReceitas );
             }

            if(pegaData > dataAtual) {
                Double totalReceitas = receitaTotal + valorRecuperado;
                movimentacao.setFlag("noDebiteado");
                atualizarReceita( totalReceitas );
            }





            movimentacao.salvar(data);
            finish();
        }
    }

    public Boolean validarCamposReceitas(){

        String textValor = campoValor.getText().toString();
        String textData = campoData.getText().toString();
        String textCategoria = campoCategoria.getText().toString();
        String textDescricao = campoDescricao.getText().toString();

        if (!textValor.isEmpty()){
            if (!textData.isEmpty()){
                if (!textCategoria.isEmpty()){
                    if (!textDescricao.isEmpty()){
                        return true;
                    }else {
                        Toast.makeText(ReceitasActivity.this,
                                "A descrição nao foi preenchida!",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                }else {
                    Toast.makeText(ReceitasActivity.this,
                            "A categoria não foi preenchida!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

            }else {
                Toast.makeText(ReceitasActivity.this,
                        "Preencha a data!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(ReceitasActivity.this,
                    "Preencha o valor da despesa!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void recuperaReceitaTotal(){
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
    public void atualizarReceita( Double receita){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.child("receitaTotal").setValue(receita);
    }
}

