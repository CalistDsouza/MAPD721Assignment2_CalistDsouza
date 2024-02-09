package com.example.mapd721_a2_calistdsouza

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapd721_a2_calistdsouza.ui.theme.MAPD721_A2_CalistDsouzaTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAPD721_A2_CalistDsouzaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(android.graphics.Color.parseColor("#E1BEE7"))
                ) {
                    MainScreen(context = this)
                }
            }
        }
    }
}


@Composable
fun MainScreen(context: ComponentActivity) {
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Assignment 2", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Contact Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { contacts = loadContacts(context) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Load")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {if (contactName.isNotBlank() && contactNumber.isNotBlank()) {
                val newContact = Contact(contactName, contactNumber)
                contacts += newContact
                addContact(context, newContact)
            } }, modifier = Modifier.weight(1f)) {
                Text("Save")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ContactsList(contacts = contacts)
        Spacer(modifier = Modifier.height(16.dp))
        AboutSection()
    }
}
@Composable
fun ContactsList(contacts: List<Contact>) {
    if (contacts.isEmpty()) {
        Text(text = "No contacts available")
    } else {
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }
}
fun addContact(context: ComponentActivity, contact: Contact) {
    // Prepare the values for insertion
    val rawContactUri: Uri =
        context.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, ContentValues())
            ?: return
    val rawContactId = ContentUris.parseId(rawContactUri)

    val values = ContentValues().apply {
        put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        put(
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        )
        put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.displayName)
    }

    val numberValues = ContentValues().apply {
        put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        put(
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        )
        put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
    }
    context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
    context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, numberValues)
}
@Composable
fun ContactsList(context: ComponentActivity) {
    // State to track if it's the first time loading
    var firstTimeLoaded by remember { mutableStateOf(true) }
    // State to hold the list of contacts
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }

    // LaunchedEffect to perform data loading
    LaunchedEffect(firstTimeLoaded) {
        if (firstTimeLoaded) {
            // Load contacts when it's the first time
            contacts = loadContacts(context)
            firstTimeLoaded = false
        }
    }

    // Display the list of contacts or a message if the list is empty
    if (contacts.isEmpty()) {
        Text(text = "No contacts available")
    } else {
        LazyColumn {
            // Display each contact in a row
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = contact.displayName)
            Text(text = contact.phoneNumber)
        }
    }
}


@Composable
fun AboutSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "About", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Name: Calist Dsouza")
        Text(text = "Student number: 301359253")
    }
}

// Data class to represent a contact
data class Contact(val displayName: String, val phoneNumber: String)

@SuppressLint("Range")
fun loadContacts(context: ComponentActivity): List<Contact> {
    val contacts = mutableListOf<Contact>()
    context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val displayName =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contacts.add(Contact(displayName, phoneNumber))
        }
    }
    return contacts
}
@Preview(showBackground = true)
@Composable
fun ContactsListPreview() {
    // Display a preview of the contact list
    MAPD721_A2_CalistDsouzaTheme {
        ContactsList(context = ComponentActivity())
    }
}
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MAPD721_A2_CalistDsouzaTheme {
        MainScreen(context = ComponentActivity())
    }
}