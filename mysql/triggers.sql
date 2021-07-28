use round_shave;

DELIMITER //


CREATE TRIGGER appointment_validation
BEFORE INSERT
   ON appointment FOR EACH ROW
BEGIN
IF NEW.start_time >= NEW.end_time THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'end_time must be after start_time';
END IF;
END; //


CREATE TRIGGER appointment_time_validation
BEFORE INSERT
   ON appointment FOR EACH ROW
BEGIN
IF exists (select 1 from appointment where
    NEW.start_time = start_time OR (NEW.start_time < start_time AND
    NEW.end_time > start_time) OR (start_time < NEW.start_time AND
     end_time > NEW.start_time)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'this time is already taken';
END IF;
END; //


DELIMITER ;
