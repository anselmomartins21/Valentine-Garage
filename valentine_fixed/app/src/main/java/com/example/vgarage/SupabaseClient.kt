package com.example.vgarage

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * SupabaseClient — creates ONE shared connection to the database.
 * Using "object" makes it a singleton: created once, reused everywhere.
 * Every other file imports this to get the database connection.
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://dkpoeydyafoxkprgfxgs.supabase.co",
        supabaseKey = "sb_publishable_Uzjc2jTmnT4co-kt9FOt2g_JL03LHse"
    ) {
        install(Postgrest) // enables database queries
    }
}
