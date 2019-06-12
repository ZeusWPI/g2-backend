-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(name, zeusID, access_token)
VALUES (:name, :zeus-id, :access-token)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name get-user-on-zeusid :? :1
SELECT * FROM users
WHERE zeusID = :zeus-id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id

/*
  Repository Providers
*/

-- :name create-repo-provider! :! :n
-- :doc creates a new link to a repository provider
INSERT INTO repository_providers
(name, access_token)
VALUES (:name, :access_token)

-- :name update-repo-provider-access-token! :! :n
UPDATE repository_providers
SET access_token = :access_token
WHERE name = :name

-- :name get-repo-provider :? :1
-- :doc retrieves a link to a repo provider
SELECT * FROM repository_providers
WHERE name = :name

-- :name get-all-repo-providers :? :*
SELECT * FROM repository_providers

/*
  Repositories
*/

-- :name create-repo! :! :1
INSERT INTO repos
(git_id, name, description, url)
VALUES (:id, :name, :description, :url)

-- :name get-repos :? :*
SELECT * FROM repos

-- :name get-repo :? :1
SELECT * FROM repos
WHERE id = :id

-- :name update-repo! :! :n
UPDATE repos
SET name = :name, description = :description, url = :url
WHERE git_id = :git_id
