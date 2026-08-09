package com.reelshelf.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.reelshelf.app.urls.Platform

class ReelShelfConverters {
    @TypeConverter
    fun toPlatform(value: String): Platform = Platform.valueOf(value)

    @TypeConverter
    fun fromPlatform(value: Platform): String = value.name

    @TypeConverter
    fun toWatchStatus(value: String): WatchStatus = WatchStatus.valueOf(value)

    @TypeConverter
    fun fromWatchStatus(value: WatchStatus): String = value.name

    @TypeConverter
    fun toReplyStatus(value: String): ReplyStatus = ReplyStatus.valueOf(value)

    @TypeConverter
    fun fromReplyStatus(value: ReplyStatus): String = value.name

    @TypeConverter
    fun toSourceApp(value: String): SourceApp = SourceApp.valueOf(value)

    @TypeConverter
    fun fromSourceApp(value: SourceApp): String = value.name
}

@Database(
    entities = [
        ClipEntity::class,
        ShareRecordEntity::class,
        SenderEntity::class,
        IngestionEventEntity::class,
        CategoryEntity::class,
        ClipCategoryCrossRef::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(ReelShelfConverters::class)
abstract class ReelShelfDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao

    abstract fun shareDao(): ShareDao

    abstract fun senderDao(): SenderDao

    abstract fun ingestionEventDao(): IngestionEventDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        const val NAME = "reelshelf.db"

        fun build(context: Context): ReelShelfDatabase =
            Room.databaseBuilder(context, ReelShelfDatabase::class.java, NAME)
                .fallbackToDestructiveMigration()
                .build()

        fun buildInMemory(context: Context): ReelShelfDatabase =
            Room.inMemoryDatabaseBuilder(context, ReelShelfDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
