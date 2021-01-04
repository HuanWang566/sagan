ALTER TABLE project ADD raw_boot_config VARCHAR(255);
ALTER TABLE project ADD rendered_boot_config VARCHAR(255);

ALTER TABLE project ADD raw_overview VARCHAR(255) DEFAULT '';
ALTER TABLE project ADD rendered_overview VARCHAR(255) DEFAULT '';

ALTER TABLE project DROP COLUMN is_aggregator;
ALTER TABLE project ADD parent_project_id CHARACTER VARYING(255) DEFAULT NULL;

ALTER TABLE project ADD display_order INT NOT NULL DEFAULT 255;

CREATE TABLE project_sample_list (
  title          VARCHAR(255),
  description    VARCHAR(255),
  url            VARCHAR(255),
  display_order  INT NOT NULL,
  project_id     CHARACTER VARYING(255) NOT NULL,
  PRIMARY KEY (project_id, display_order)
);