import sqlalchemy as db
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy import Table, Column, Integer, String, Boolean, Text, ForeignKey, TIMESTAMP
from sqlalchemy.ext.declarative import declarative_base

SQLALCHEMY_DATABASE_URL = "sqlite:///./server/test.db"
engine = db.create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}) 
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

metadata = db.MetaData()
users = db.Table(
    "users",
    metadata,
    db.Column("id", db.Integer, primary_key=True, index=True),
    db.Column("phone", db.String, unique=True, index=True),
    db.Column("email", db.String, unique=True, index=True),
    db.Column("full_name", db.String),
    db.Column("hashed_password", db.String),
    db.Column("disabled", db.Boolean, default=False), 
)

SUBJECTS_DB_URL = "sqlite:///./server/user_subjects.db"
subjects_engine = db.create_engine(SUBJECTS_DB_URL, connect_args={"check_same_thread": False})
SubjectsSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=subjects_engine)

# Используем subjects_metadata для ВСЕХ таблиц предметов
subjects_metadata = db.MetaData()

user_subjects = db.Table(
    "user_subjects",
    subjects_metadata,
    db.Column("id", db.Integer, primary_key=True),
    db.Column("user_phone", db.String), 
    db.Column("subject_name", db.String),
)

# Создаем таблицы topics, quizzes, quiz_options в ТОЙ ЖЕ базе данных с ТЕМИ ЖЕ метаданными
topics = db.Table(
    "topics",
    subjects_metadata,
    db.Column("id", db.Integer, primary_key=True),
    db.Column("subject_name", db.String(100), nullable=False),
    db.Column("title", db.String(255), nullable=False),
    db.Column("content", db.Text, nullable=False),
    db.Column("created_at", db.TIMESTAMP, server_default=db.text("CURRENT_TIMESTAMP"))
)

quizzes = db.Table(
    "quizzes",
    subjects_metadata,
    db.Column("id", db.Integer, primary_key=True),
    db.Column("topic_id", db.Integer, db.ForeignKey("topics.id"), nullable=False),
    db.Column("question", db.Text, nullable=False),
    db.Column("correct_answer_index", db.Integer, nullable=False),
    db.Column("created_at", db.TIMESTAMP, server_default=db.text("CURRENT_TIMESTAMP"))
)

quiz_options = db.Table(
    "quiz_options",
    subjects_metadata,
    db.Column("id", db.Integer, primary_key=True),
    db.Column("quiz_id", db.Integer, db.ForeignKey("quizzes.id"), nullable=False),
    db.Column("option_text", db.Text, nullable=False),
    db.Column("option_index", db.Integer, nullable=False),
    db.Column("created_at", db.TIMESTAMP, server_default=db.text("CURRENT_TIMESTAMP"))
)

# Создаем таблицы в соответствующих базах данных
metadata.create_all(bind=engine)
subjects_metadata.create_all(bind=subjects_engine)

# Удаляем лишний declarative_base если он не используется
# Base = declarative_base()