-- :name get-projects :? :*
-- :doc return all projects
SELECT * from project

-- :name create-project! :! :n
-- :doc create a project
INSERT INTO projects (name, description)
VALUES (:name, :description)
