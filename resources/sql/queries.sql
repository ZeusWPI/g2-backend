-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(name, id_zeus)
VALUES (:name, :id_zeus)

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
WHERE id_zeus = :id_zeus

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

-- :name create-github-repo! :! :1
INSERT INTO github_repositories
(id, name, description)
VALUES (:id, :name, :description)

-- :name get-github-repos :? :*
SELECT * FROM github_repositories

-- :name update-github-repo! :! :n
UPDATE github_repositories
SET name = :name, description = :description
WHERE id = :id
