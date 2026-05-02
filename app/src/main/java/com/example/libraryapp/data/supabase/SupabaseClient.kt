package com.example.libraryapp.data.supabase

import com.example.libraryapp.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


val supabase = createSupabaseClient(
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    supabaseUrl = BuildConfig.SUPABASE_URL
) {
    install(Postgrest)
    install(Auth)
}