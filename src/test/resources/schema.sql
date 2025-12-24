CREATE TABLE sys_role (
  role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(20) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  role_desc VARCHAR(200)
);

CREATE TABLE sys_user (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  account VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(50) NOT NULL,
  student_no VARCHAR(30) UNIQUE,
  phone VARCHAR(20),
  nickname VARCHAR(50),
  avatar_url VARCHAR(255),
  status TINYINT NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE activity (
  activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  location VARCHAR(100),
  description TEXT,
  cover_url VARCHAR(255),
  start_time DATETIME,
  end_time DATETIME,
  signup_start DATETIME,
  signup_end DATETIME,
  status TINYINT NOT NULL,
  capacity INT,
  signed_count INT NOT NULL DEFAULT 0,
  is_volunteer TINYINT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT NOT NULL
);

CREATE TABLE activity_registration (
  reg_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  student_no VARCHAR(30) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL,
  audit_reason VARCHAR(200),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uk_activity_user UNIQUE (activity_id, user_id)
);

CREATE INDEX idx_activity ON activity_registration(activity_id);
CREATE INDEX idx_user ON activity_registration(user_id);
CREATE INDEX idx_student_no ON activity_registration(student_no);

CREATE TABLE attendance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  student_no VARCHAR(30) NOT NULL,
  check_status TINYINT NOT NULL,
  check_in_time DATETIME,
  check_out_time DATETIME,
  duration_minutes INT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uk_activity_user_att UNIQUE (activity_id, user_id)
);

CREATE TABLE notification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(500) NOT NULL,
  read_flag TINYINT NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE survey_template (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  enabled TINYINT NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE survey_question (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_id BIGINT NOT NULL,
  question_text VARCHAR(300) NOT NULL,
  question_type VARCHAR(20) NOT NULL,
  required_flag TINYINT NOT NULL,
  sort_no INT NOT NULL
);

CREATE TABLE survey_response (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,
  rating_score INT NOT NULL,
  suggestion_text VARCHAR(500),
  created_at DATETIME NOT NULL,
  CONSTRAINT uk_activity_user_survey UNIQUE (activity_id, user_id)
);
