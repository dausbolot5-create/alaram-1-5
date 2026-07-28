package com.example.examping.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.TriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY tanggal ASC, jamMulai ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: String): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String)

    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()

    @Query("SELECT * FROM triggers")
    fun getAllTriggers(): Flow<List<TriggerEntity>>

    @Query("SELECT * FROM triggers WHERE examId = :examId")
    suspend fun getTriggersForExam(examId: String): List<TriggerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriggers(triggers: List<TriggerEntity>)

    @Query("UPDATE triggers SET sudahBunyi = :sudahBunyi WHERE id = :id")
    suspend fun markTriggerFired(id: String, sudahBunyi: Boolean = true)

    @Query("DELETE FROM triggers WHERE examId = :examId")
    suspend fun deleteTriggersForExam(examId: String)

    @Query("DELETE FROM triggers")
    suspend fun deleteAllTriggers()
}
