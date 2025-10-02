from pydantic import BaseModel
from datetime import datetime, timedelta
from jose import jwt
from passlib.context import CryptContext
from typing import Optional
import sqlalchemy as db
from sqlalchemy.orm import Session
from config import SECRET_KEY, ALGORITHM
from Kdb import *


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

class UserCreate(BaseModel):
    phone: str
    email: str
    full_name: str
    password: str

class UserInDB(BaseModel): 
    phone: str
    email: str
    full_name: str
    hashed_password: str
    disabled: Optional[bool] = None

class User(BaseModel):
    phone: str
    email: str
    full_name: str

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    phone: Optional[str] = None

class Subject(BaseModel):
    id: int
    name: str

class UserSubjectCreate(BaseModel):
    subject_name: str

class UserSubject(BaseModel):
    id: int
    user_phone: str
    subject_name: str

class TwoStepAuth:
    def __init__(self):
        self.pending_auth = {}

class ChatRequest(BaseModel):
    message: str
    is_sync: Optional[bool] = True 

class ChatResponse(BaseModel):
    response: str

class LoginStep1Response(BaseModel):
    message: str = "Please enter your password"

class LoginStep2Response(BaseModel):
    auth_token: str
    message: str = "Login successful"
    
def verify_password(plain_password: str, hashed_password: str):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str):
    return pwd_context.hash(password)

def get_user(db_session: Session, phone: str):
    query = db.select(users).where(users.c.phone == phone)
    result = db_session.execute(query).fetchone()
    if result:
        return {
        "phone": result.phone,
        "email": result.email,
        "full_name": result.full_name,
        "hashed_password": result.hashed_password,
        "disabled": result.disabled
    }

def authenticate_user(db_session: Session, phone: str, password: str):
    user = get_user(db_session, phone)
    if not user:
        return False
    if not verify_password(password, user["hashed_password"]):
        return False
    return user

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def get_db():
    db_session = SessionLocal()
    try:
        yield db_session
    finally:
        db_session.close()
