package com.example.smartnotesapp.repository

import com.example.smartnotesapp.data.Note
import com.example.smartnotesapp.data.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
}
