from fastapi import FastAPI, Depends, HTTPException, status, Request, Form, File, UploadFile
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from datetime import datetime, timedelta
#from jose import JWTError, jwt
import jwt
from jwt.exceptions import InvalidTokenError
from passlib.context import CryptContext
from typing import Optional, List, Dict, Any
import sqlalchemy as db
from sqlalchemy.orm import sessionmaker, Session
import requests
from server.config import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES, API_KEY
from server.Kdb import *
from server.class_and_def import *
from openai import *
from server.quest_for_gpt import *
from fastapi.responses import JSONResponse
import json
app = FastAPI()
two_step_auth = TwoStepAuth()
current_phone = None
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token") #хз надо нет
@app.post("/signup", response_model=User)
def signup(user: UserCreate, db_session: Session = Depends(get_db)):
    existing_user_phone = get_user(db_session, user.phone)
    existing_user_email = db_session.execute(
    db.select(users).where(users.c.email == user.email) 
    ).fetchone()
    
    if existing_user_phone:
        raise HTTPException(status_code=400, detail="Phone number already registered")
    if existing_user_email:
        raise HTTPException(status_code=400, detail="Email already registered")

    hashed_password = get_password_hash(user.password)
    
    query = users.insert().values(
        phone=user.phone,
        email=user.email,
        full_name=user.full_name,
        hashed_password=hashed_password,
        disabled=False
    )
    db_session.execute(query)
    db_session.commit()
    
    return {"phone": user.phone, "email": user.email, "full_name": user.full_name}
@app.post("/token") #не используеться
async def login_for_token(
    phone: str = Form(...),
    password: str = Form(...),
    db_session: Session = Depends(get_db)
):
    user = authenticate_user(db_session, phone, password)
    if not user:
        raise HTTPException(401, "Invalid credentials")
    return {"access_token": "zxc", "token_type": "bearer"}

@app.post("/login/step1", response_model=LoginStep1Response)
async def login_step1(
    phone: str = Form(...),
    db_session: Session = Depends(get_db)
):
    global current_phone
    user = get_user(db_session, phone)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    current_phone = phone  
    return {"message": "Please enter your password"}

@app.post("/login/step2", response_model=LoginStep2Response)
async def login_step2(
    password: str = Form(...),
    db_session: Session = Depends(get_db)
):
    global current_phone
    
    if not current_phone:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Complete step 1 first"
        )

    user = authenticate_user(db_session, current_phone, password)
    if not user:
        current_phone = None 
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect password"
        )
    current_phone = None
    return {
        "auth_token": "zxc"
    }

@app.get("/users/me", response_model=User)
async def read_users_me(
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    if auth_token != "zxc":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication"
        )
    query = db.select(users)
    user = db_session.execute(query).fetchone()
    if not user:
        raise HTTPException(status_code=404, detail="No users found")
    
    return {
        "phone": user.phone,
        "email": user.email,
        "full_name": user.full_name
    }

@app.post("/subjects/add")
def add_subject_for_user(
    subject_in: UserSubjectCreate,
    auth_token: str,
    db_session_users: Session = Depends(get_db)):

    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Invalid authentication")

    phone = "1234567890"

    with SubjectsSessionLocal() as sdb:
        query_check = db.select(user_subjects).where( 
            (user_subjects.c.user_phone == phone) & 
            (user_subjects.c.subject_name == subject_in.subject_name)
        )
        result = sdb.execute(query_check).fetchone()
        
        if result:
            raise HTTPException(status_code=400, detail="Subject already added for this user")
        
        insert_query = db.insert(user_subjects).values(
            user_phone=phone,
            subject_name=subject_in.subject_name
        )
        sdb.execute(insert_query)
        sdb.commit()
        
    return {"message": "Subject added successfully"}

@app.delete("/subjects/delete/{subject_name}")
def delete_subject(
    subject_name: str,
    auth_token: str,
    db_session_users: Session = Depends(get_db)
):
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Invalid authentication")

    # For demo purposes, just use a dummy phone
    phone = "1234567890"

    with SubjectsSessionLocal() as sdb:
        query_check = db.select(user_subjects).where(
            (user_subjects.c.user_phone == phone) &
            (user_subjects.c.subject_name == subject_name)
        )
        result = sdb.execute(query_check).fetchone()  
        if not result:
            raise HTTPException(
                status_code=404,
                detail="Subject not found or you don't have permission to delete it"
            )
        delete_query = db.delete(user_subjects).where(
            (user_subjects.c.user_phone == phone) &
            (user_subjects.c.subject_name == subject_name)
        )
        sdb.execute(delete_query)
        sdb.commit()
        
    return {"message": "Subject deleted successfully"}

@app.get("/subjects/", response_model=List[Subject])
def get_user_subjects(
    auth_token: str,
    db_session_users: Session = Depends(get_db)
):
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")

    phone = "1234567890"

    with SubjectsSessionLocal() as sdb:
        user_subjects_list = sdb.execute(
            db.select(user_subjects.c.subject_name)
            .where(user_subjects.c.user_phone == phone)
        ).fetchall()
        
        subjects = [
            {"id": idx+1, "name": subj.subject_name} 
            for idx, subj in enumerate(user_subjects_list)
        ]
        
        return subjects

@app.post("/chat", response_model=ChatResponse)
async def chat_with_ai(
    chat_request: ChatRequest,
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")
    input_data = {
        "is_sync": chat_request.is_sync,
        "messages": [
            {
                "role": "user",
                "content": chat_request.message
            }
        ]
    }
    
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': f'Bearer {API_KEY}'
    }
    
    url_endpoint = "https://api.gen-api.ru/api/v1/networks/deepseek-v3"
    try:
        response = requests.post(
            url_endpoint,
            json=input_data,
            headers=headers,
            timeout=30 
        )
        response.raise_for_status()
        ai_response = response.json()

        if isinstance(ai_response, dict):
            if 'response' in ai_response and ai_response['response']:
                first_response = ai_response['response'][0]
                if 'choices' in first_response and first_response['choices']:
                    message = first_response['choices'][0].get('message', {})
                    return {"response": message.get('content', 'Ответ пуст')}
        
        return {"response": "Не удалось обработать ответ от AI"}
        
    except requests.exceptions.RequestException as e:
        print(f"Ошибка запроса к AI: {str(e)}")
        raise HTTPException(
            status_code=502,
            detail="Сервис AI временно недоступен"
        )
    except Exception as e:
        print(f"Неожиданная ошибка: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail="Внутренняя ошибка сервера"
        )
   


# Добавьте эти эндпоинты в ваш существующий код:

from sqlalchemy.sql import text
import json
from typing import Dict, Any, List
import tempfile
from fastapi.responses import FileResponse

@app.get("/subjects/all", response_model=List[SubjectResponse])
def get_all_subjects_with_topics(
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    """
    не работает но вроде и не надо пока пользуйтесь /subjects/json
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")

    try:
        # Получаем предметы пользователя
        phone = "1234567890"
        
        with SubjectsSessionLocal() as sdb:
            # Получаем предметы пользователя
            user_subjects_list = sdb.execute(
                db.select(user_subjects.c.subject_name)
                .where(user_subjects.c.user_phone == phone)
            ).fetchall()
            
            subjects_with_topics = []
            
            for idx, user_subj in enumerate(user_subjects_list):
                subject_id = idx + 1
                subject_name = user_subj.subject_name
                
                # Получаем темы для каждого предмета
                topics_list = sdb.execute(
                    db.select(topics).where(topics.c.subject_name == subject_name)
                ).fetchall()
                
                topics = []
                for topic in topics_list:
                    # Получаем квиз для темы
                    quiz_data = sdb.execute(
                        db.select(quizzes).where(quizzes.c.topic_id == topic.id)
                    ).fetchone()
                    
                    quiz = None
                    if quiz_data:
                        # Получаем опции для квиза
                        options_data = sdb.execute(
                            db.select(quiz_options).where(quiz_options.c.quiz_id == quiz_data.id)
                            .order_by(quiz_options.c.option_index)
                        ).fetchall()
                        
                        options = [opt.option_text for opt in options_data]
                        
                        quiz = QuizResponse(
                            question=quiz_data.question,
                            options=options,
                            correctAnswerIndex=quiz_data.correct_answer_index
                        )
                    
                    topic_response = TopicResponse(
                        id=topic.id,
                        title=topic.title,
                        content=topic.content,
                        quiz=quiz
                    )
                    topics.append(topic_response)
                
                subject = SubjectResponse(
                    id=subject_id,
                    name=subject_name,
                    topics=topics
                )
                subjects_with_topics.append(subject)
            
            return subjects_with_topics
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка при получении данных: {str(e)}")

@app.get("/subjects/json", response_model=Dict[str, Any])
def get_subjects_json_format(
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    """
    Эндпоинт возвращает данные в точном формате JSON как в вашем raw/data.json
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")

    try:
        # Получаем предметы пользователя напрямую, чтобы избежать рекурсии
        phone = "1234567890"
        
        with SubjectsSessionLocal() as sdb:
            # Получаем предметы пользователя
            user_subjects_list = sdb.execute(
                db.select(user_subjects.c.subject_name)
                .where(user_subjects.c.user_phone == phone)
            ).fetchall()
            
            response_data = {"subjects": []}
            
            for idx, user_subj in enumerate(user_subjects_list):
                subject_id = idx + 1
                subject_name = user_subj.subject_name
                
                # Получаем темы для предмета
                topics_list = sdb.execute(
                    db.select(topics).where(topics.c.subject_name == subject_name)
                ).fetchall()
                
                topics_data = []
                for topic in topics_list:
                    # Получаем квиз для темы
                    quiz_data = sdb.execute(
                        db.select(quizzes).where(quizzes.c.topic_id == topic.id)
                    ).fetchone()
                    
                    quiz_info = None
                    if quiz_data:
                        # Получаем опции для квиза
                        options_data = sdb.execute(
                            db.select(quiz_options).where(quiz_options.c.quiz_id == quiz_data.id)
                            .order_by(quiz_options.c.option_index)
                        ).fetchall()
                        
                        options = [opt.option_text for opt in options_data]
                        
                        quiz_info = {
                            "question": quiz_data.question,
                            "options": options,
                            "correctAnswerIndex": quiz_data.correct_answer_index
                        }
                    
                    topic_info = {
                        "id": topic.id,
                        "title": topic.title,
                        "content": topic.content,
                        "quiz": quiz_info
                    }
                    topics_data.append(topic_info)
                
                subject_info = {
                    "id": subject_id,
                    "name": subject_name,
                    "topics": topics_data
                }
                response_data["subjects"].append(subject_info)
        
        return response_data
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка при формировании JSON: {str(e)}")

@app.get("/export/subjects-json")
async def export_subjects_json(
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    """
    Эндпоинт для экспорта всех данных в JSON файле
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")

    try:
        json_data = get_subjects_json_format(auth_token, db_session)
        
        # Возвращаем как файл для скачивания
        with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False, encoding='utf-8') as f:
            json.dump(json_data, f, ensure_ascii=False, indent=2)
            temp_path = f.name
        
        return FileResponse(
            temp_path, 
            media_type='application/json',
            filename="subjects_export.json"
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка при экспорте: {str(e)}")

@app.post("/subjects/create-from-json")
async def create_subjects_from_json(
    subjects_data: SubjectsDataCreate,
    auth_token: str,
    db_session: Session = Depends(get_db)
):
    """
    Эндпоинт для создания предметов и тем из JSON данных в теле запроса
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")

    try:
        phone = "1234567890"
        
        with SubjectsSessionLocal() as sdb:
            subjects_processed = 0
            topics_processed = 0
            quizzes_processed = 0
            
            # Обрабатываем каждый предмет из JSON
            for subject_data in subjects_data.subjects:
                subject_name = subject_data.name
                
                # Проверяем, есть ли уже такой предмет у пользователя
                existing_subject = sdb.execute(
                    db.select(user_subjects).where(
                        (user_subjects.c.user_phone == phone) & 
                        (user_subjects.c.subject_name == subject_name)
                    )
                ).fetchone()
                
                if not existing_subject:
                    # Добавляем предмет
                    insert_subject_query = db.insert(user_subjects).values(
                        user_phone=phone,
                        subject_name=subject_name
                    )
                    sdb.execute(insert_subject_query)
                    subjects_processed += 1
                
                # Добавляем темы и квизы
                for topic_data in subject_data.topics:
                    # Проверяем, есть ли уже такая тема
                    existing_topic = sdb.execute(
                        db.select(topics).where(
                            (topics.c.subject_name == subject_name) & 
                            (topics.c.title == topic_data.title)
                        )
                    ).fetchone()
                    
                    if not existing_topic:
                        # Добавляем тему
                        insert_topic_query = db.insert(topics).values(
                            subject_name=subject_name,
                            title=topic_data.title,
                            content=topic_data.content
                        )
                        result = sdb.execute(insert_topic_query)
                        topic_id = result.lastrowid
                        topics_processed += 1
                        
                        # Добавляем квиз если есть
                        if topic_data.quiz:
                            quiz_data = topic_data.quiz
                            insert_quiz_query = db.insert(quizzes).values(
                                topic_id=topic_id,
                                question=quiz_data.question,
                                correct_answer_index=quiz_data.correctAnswerIndex
                            )
                            quiz_result = sdb.execute(insert_quiz_query)
                            quiz_id = quiz_result.lastrowid
                            quizzes_processed += 1
                            
                            # Добавляем опции квиза
                            for option_idx, option_text in enumerate(quiz_data.options):
                                insert_option_query = db.insert(quiz_options).values(
                                    quiz_id=quiz_id,
                                    option_text=option_text,
                                    option_index=option_idx
                                )
                                sdb.execute(insert_option_query)
                
                sdb.commit()
        
        return {
            "message": "Данные успешно обработаны",
            "subjects_processed": subjects_processed,
            "topics_processed": topics_processed,
            "quizzes_processed": quizzes_processed
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка при обработке данных: {str(e)}")

# Дополнительные полезные эндпоинты

@app.get("/subjects/check-data")
async def check_subjects_data(auth_token: str):
    """
    Проверить какие данные есть в базе
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")
    
    try:
        phone = "1234567890"
        
        with SubjectsSessionLocal() as sdb:
            # Получаем предметы
            user_subjects_count = sdb.execute(
                db.select(db.func.count()).select_from(user_subjects)
                .where(user_subjects.c.user_phone == phone)
            ).scalar()
            
            # Получаем темы
            topics_count = sdb.execute(
                db.select(db.func.count()).select_from(topics)
            ).scalar()
            
            # Получаем квизы
            quizzes_count = sdb.execute(
                db.select(db.func.count()).select_from(quizzes)
            ).scalar()
            
            return {
                "user_subjects_count": user_subjects_count,
                "topics_count": topics_count,
                "quizzes_count": quizzes_count,
                "user_phone": phone
            }
            
    except Exception as e:
        return {"error": str(e)}

@app.post("/subjects/clear-data")
async def clear_subjects_data(auth_token: str):
    """
    Очистить все данные (для тестирования)
    """
    if auth_token != "zxc":
        raise HTTPException(status_code=401, detail="Неверная аутентификация")
    
    try:
        with SubjectsSessionLocal() as sdb:
            # Очищаем в правильном порядке из-за foreign keys
            sdb.execute(db.delete(quiz_options))
            sdb.execute(db.delete(quizzes))
            sdb.execute(db.delete(topics))
            sdb.execute(db.delete(user_subjects))
            sdb.commit()
            
        return {"message": "Все данные очищены"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка при очистке данных: {str(e)}")