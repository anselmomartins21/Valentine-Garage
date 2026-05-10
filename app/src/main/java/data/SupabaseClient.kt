package data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://dkpoeydyafoxkprgfxgs.supabase.co/rest/v1/",
        supabaseKey = "sb_publishable_Uzjc2jTmnT4co-kt9FOt2g_JL03LHse"
    ) {
        install(Postgrest)
    }
}