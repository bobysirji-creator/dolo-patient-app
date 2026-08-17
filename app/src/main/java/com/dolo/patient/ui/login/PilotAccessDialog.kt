package com.dolo.patient.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dolo.patient.auth.AuthUiState

@Composable fun PilotAccessDialog(state:AuthUiState,onMode:(Boolean)->Unit,onDoloId:(String)->Unit,onInvite:(String)->Unit,onCredential:(String)->Unit,onSubmit:()->Unit,onDismiss:()->Unit){
 AlertDialog(onDismissRequest=onDismiss,confirmButton={Button(onClick=onSubmit,enabled=state.pilotReadiness?.enabled==true&&!state.isLoading&&state.pilotCredential.length>=8&&(if(state.pilotActivation)state.pilotInviteCode.length==32 else state.pilotDoloId.matches(Regex("^DLO-PAT-[0-9]{6}$")))){Text(if(state.isLoading)"Connecting..." else if(state.pilotActivation)"Activate account" else "Sign in")}},dismissButton={TextButton(onDismiss){Text("Cancel")}},title={Text("Controlled pilot access")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("Invitation-only testing. Open registration and demo fallback are disabled in this mode.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(!state.pilotActivation,{onMode(false)},{Text("Sign in")});FilterChip(state.pilotActivation,{onMode(true)},{Text("Activate invite")})}
  if(state.pilotActivation)OutlinedTextField(state.pilotInviteCode,onInvite,Modifier.fillMaxWidth(),label={Text("32-character invitation code")},singleLine=true)
  else OutlinedTextField(state.pilotDoloId,onDoloId,Modifier.fillMaxWidth(),label={Text("Patient DO-LO ID")},singleLine=true)
  OutlinedTextField(state.pilotCredential,onCredential,Modifier.fillMaxWidth(),label={Text(if(state.pilotActivation)"Create private credential" else "Private credential")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
  state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
 }})
}