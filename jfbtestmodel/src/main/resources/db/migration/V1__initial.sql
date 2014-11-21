CREATE TABLE "position"
(
  id bigint NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT position_pkey PRIMARY KEY (id),
  CONSTRAINT position_name_key UNIQUE (name)
);

CREATE TABLE priveledge
(
  id bigint NOT NULL,
  name VARCHAR(255),
  CONSTRAINT priveledge_pkey PRIMARY KEY (id)
);

CREATE TABLE employee
(
  id bigint NOT NULL,
  hired_at date,
  name VARCHAR(255),
  department_id bigint NOT NULL,
  position_id bigint,
  CONSTRAINT employee_pkey PRIMARY KEY (id),
  CONSTRAINT fk_employee_position_id FOREIGN KEY (position_id)
    REFERENCES "position" (id)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE department
(
  id bigint NOT NULL,
  name VARCHAR(255) NOT NULL,
  head_id bigint,
  CONSTRAINT department_pkey PRIMARY KEY (id),
  CONSTRAINT fk_department_head_id FOREIGN KEY (head_id)
  REFERENCES employee (id)
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT department_name_key UNIQUE (name)
);

ALTER TABLE employee
  ADD CONSTRAINT fk_employee_department_id FOREIGN KEY (department_id)
    REFERENCES department (id)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE TABLE employee_priveledge
(
  employee_id bigint NOT NULL,
  priveledge_id bigint NOT NULL,
  CONSTRAINT employee_priveledges_pkey PRIMARY KEY (employee_id, priveledge_id),
  CONSTRAINT fk_employee_priveledges_employee_id FOREIGN KEY (employee_id)
    REFERENCES employee (id)
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_employee_priveledges_priveledge_id FOREIGN KEY (priveledge_id)
    REFERENCES priveledge (id)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);



INSERT INTO "position" VALUES (8, 'CEO');
INSERT INTO "position" VALUES (10, 'Junior developer');
INSERT INTO "position" VALUES (11, 'Analyst');
INSERT INTO "position" VALUES (9, 'Senior developer');
INSERT INTO "position" VALUES (12, 'Tech fellow');

INSERT INTO priveledge VALUES (7, 'Additional day off');
INSERT INTO priveledge VALUES (6, 'Free lunch');
INSERT INTO priveledge VALUES (5, 'Parking');

INSERT INTO department VALUES (4, 'Analytics', NULL);
INSERT INTO department VALUES (1, 'Development', NULL);
INSERT INTO department VALUES (2, 'Marketing', NULL);
INSERT INTO department VALUES (3, 'Sales', NULL );

INSERT INTO employee VALUES (13, NULL, 'John White', 1, 9);
INSERT INTO employee VALUES (18, NULL, 'Paul Dukas', 3, 10);
INSERT INTO employee VALUES (19, NULL, 'Jimmy Carter', 2, 11);
INSERT INTO employee VALUES (20, NULL, 'Simon Cowell', 4, 9);
INSERT INTO employee VALUES (21, NULL, 'John Mellencamp', 1, 12);
INSERT INTO employee VALUES (22, NULL, 'Peter Max', 2, 11);
INSERT INTO employee VALUES (23, NULL, 'Carlo Urbani', 3, 9);
INSERT INTO employee VALUES (24, NULL, 'Jim Rogers', 4, 11);
INSERT INTO employee VALUES (14, '2012-01-15', 'Bill Carry', 1, 10);
INSERT INTO employee VALUES (15, '2013-11-14', 'Alexander Bain', 2, 11);
INSERT INTO employee VALUES (16, '2013-07-30', 'Randy Quaid', 3, 12);
INSERT INTO employee VALUES (17, '2014-02-02', 'Richard Harris', 4, 8);

UPDATE department SET head_id = 13 WHERE id = 1;
UPDATE department SET head_id = 15 WHERE id = 2;
UPDATE department SET head_id = 18 WHERE id = 3;

INSERT INTO employee_priveledge VALUES (13, 6);
INSERT INTO employee_priveledge VALUES (14, 5);
INSERT INTO employee_priveledge VALUES (14, 6);
INSERT INTO employee_priveledge VALUES (15, 5);
INSERT INTO employee_priveledge VALUES (15, 6);
INSERT INTO employee_priveledge VALUES (16, 7);
