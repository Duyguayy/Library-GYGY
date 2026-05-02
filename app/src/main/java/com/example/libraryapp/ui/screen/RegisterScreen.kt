package com.example.libraryapp.ui.screen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.libraryapp.ui.viewmodel.AuthState
import com.example.libraryapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var fullName  by remember { mutableStateOf("") }
    var studentNo by remember { mutableStateOf("") }

    // ── ÖDEV 1: Success yapısı ────────────────────────────────────────────────────────
    // LoginScreen'deki LaunchedEffect(authState) kalıbının aynısı.
    // signUp() → AuthState.Success geldiğinde:
    //   1. Yeşil banner ekranın üstünden kayar.
    //   2. 1.5 sn bekler.
    //   3. State sıfırlanır → Login ekranına gidilir.
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessBanner = true
            delay(1_500L)
            authViewModel.resetState()
            onNavigateToLogin()
        }
    }
    // ─────────────────────────────────────────────────────────────────────────────────

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "Kayıt Ol",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value         = fullName,
                onValueChange = { fullName = it },
                label         = { Text("Ad Soyad") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                enabled       = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it },
                label           = { Text("E-posta") },
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine      = true,
                enabled         = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value                  = password,
                onValueChange          = { password = it },
                label                  = { Text("Şifre") },
                modifier               = Modifier.fillMaxWidth(),
                visualTransformation   = PasswordVisualTransformation(),
                keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine             = true,
                enabled                = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value           = studentNo,
                onValueChange   = { studentNo = it },
                label           = { Text("Öğrenci No (opsiyonel)") },
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine      = true,
                enabled         = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Error) {
                Text(
                    text     = (authState as AuthState.Error).message,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    authViewModel.signUp(
                        email     = email.trim(),
                        password  = password,
                        fullName  = fullName.trim(),
                        studentNo = studentNo.trim().ifEmpty { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Kayıt Ol")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { onNavigateToLogin() }) {
                Text("Zaten hesabın var mı? Giriş Yap")
            }
        }

        // ── ÖDEV 1: Başarı Banner'ı (ekranın üstünden kayarak gelir) ─────────────────
        AnimatedVisibility(
            visible  = showSuccessBanner,
            enter    = fadeIn(tween(350)) + slideInVertically(tween(350)) { -80 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp, start = 24.dp, end = 24.dp)
        ) {
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = "Başarı",
                        tint               = Color.White,
                        modifier           = Modifier.size(30.dp)
                    )
                    Column {
                        Text(
                            text       = "Kayıt Başarılı! 🎉",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        Text(
                            text     = "Giriş ekranına yönlendiriliyorsunuz…",
                            color    = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────────────────
    }
}