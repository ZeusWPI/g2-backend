/*
  We try to hold onto the ANSI standard. Any deviations from this standard should be documented above the query.

Global deviations
-----------------

Autoincrementing primary keys: The standard is pretty bad and verbose. Sqlite autoincrements 'primary key integers'. MySQL does the same using 'SERIAL primary key'
*/

/*
 Generic Queries
 */

-- :name update-generic! :! :n
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
UPDATE :i:table
SET
/*~
(string/join ","
  (for [[field _] (:updates params)]
    (str (identifier-param-quote (name field) options)
      " = :v:updates." (name field))))
~*/
WHERE tag_id = :id;

-- :name create-generic! :! :n
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
INSERT INTO :i:table
(
/*~
(string/join ","
  (for [[field _] (:data params)]
    (str (identifier-param-quote (name field) options))))
~*/
)
VALUES
    (
/*~
(string/join ","
  (for [[field _] (:data params)]
    (str " :v:data." (name field))))
~*/
    );

/*
    Entity queries
*/
-- :name get-tag :? :1
SELECT *
FROM :i:table
WHERE tag_id = :tag_id;

-- :name get-tags :? :*
SELECT *
FROM :i:table;


-- :name delete-entity! :! :1
DELETE
FROM :i:table
WHERE tag_id = :tag_id;

-- :name count-entity :? :1
SELECT count(*) as count
FROM :i:table
WHERE tag_id = :tag_id;

-- :name get-tags-linked-with-tag :? :*
SELECT *
FROM tag_relations
         INNER JOIN :i:table
ON child_id = tag_id
WHERE parent_id = :tag_id;

-- :name get-tags-count-linked-with-tag :? :1
SELECT count(*) as count
FROM tag_relations
         INNER JOIN :i:table
ON child_id = tag_id
WHERE parent_id = :tag_id;

/*
    Tag table
 */
-- :name create-tag! :insert :raw
INSERT INTO tags ()
VALUES ();

-- :name delete-tag! :! :1
DELETE
FROM tags
WHERE id = :id;

-- :name set-feature-tag! :! :n
UPDATE tags
set featured = :featured
WHERE id = :tag_id;


-- :name link-tag! :! :n
INSERT INTO tag_relations
    (parent_id, child_id)
VALUES (:parent_id, :child_id);

-- :name unlink-tag! :! :n
DELETE
FROM tag_relations
WHERE parent_id = :parent_id
  and child_id = :child_id;

/*
    User stuff
 */

-- :name create-user! :insert :raw
INSERT INTO users
    (name, email, admin, last_login)
VALUES (:name, :email, :admin, :last_login);

-- :name get-user :? :1
SELECT *
FROM users
         LEFT OUTER JOIN zeus_users using (user_id)
WHERE user_id = :user_id;

-- :name get-user-on-zeusid :? :1
SELECT *
FROM users
         LEFT JOIN zeus_users using (user_id)
WHERE zeus_id = :zeus_id;

-- :name get-users :? :*
SELECT *
FROM users
         LEFT OUTER JOIN zeus_users using (user_id);

-- :name delete-user! :! :n
DELETE
FROM users
WHERE user_id = :user_id;

/*
  Repository Providers
*/

-- :name create-repo-provider! :! :n
-- :doc creates a new link to a repository provider
INSERT INTO repository_providers
    (name, access_token)
VALUES (:name, :access_token);

-- :name update-repo-provider-access-token! :! :n
UPDATE repository_providers
SET access_token = :access_token
WHERE name = :name;

-- :name get-repo-provider :? :1
-- :doc retrieves a link to a repo provider
SELECT *
FROM repository_providers
WHERE name = :name;

-- :name get-all-repo-providers :? :*
SELECT *
FROM repository_providers;

/*
    Features
*/
-- :name get-project-features-of-type :? :*
( -- The entities directly linked to a project
    select i.*
    from :i:table i
        inner join tags t on i.tag_id = t.id
        inner join tag_relations tr on tr.child_id = i.tag_id
    where tr.parent_id = :project_id and featured = true
)
UNION
( -- The entities linked to a project via a repo
    select i.*
    from :i:table i
        inner join tags t2 on i.tag_id = t2.id
        inner join repos r on i.repo_id = r.tag_id
        inner join tag_relations tr on tr.child_id = r.tag_id
    where tr.parent_id = :project_id and featured = true
);

/*
  Repositories
*/

-- :name create-repo! :insert :raw
INSERT INTO repos
    (tag_id, git_id, name, description, url)
VALUES (:tag_id, :git_id, :name, :description, :url);

-- :name get-repos :? :*
SELECT repos.tag_id                                as id,
       repos.name                                  as name,
       repos.description,
       url,
       group_concat(named_tags.name SEPARATOR ',') as default_tags
FROM repos
         LEFT JOIN repo_default_tag_mapping ON repos.tag_id = repo_default_tag_mapping.repo_id
         LEFT JOIN named_tags ON repo_default_tag_mapping.tag_id = named_tags.tag_id
GROUP BY repos.tag_id;

-- :name get-repo :? :1
SELECT repos.tag_id                                as id,
       repos.name                                  as name,
       repos.description,
       url,
       group_concat(named_tags.name SEPARATOR ',') as default_tags
FROM repos
         JOIN repo_default_tag_mapping ON repos.tag_id = repo_default_tag_mapping.repo_id
         JOIN named_tags ON repo_default_tag_mapping.tag_id = named_tags.tag_id
WHERE repo_id = :repo_id
GROUP BY repos.tag_id;


-- :name update-repo! :! :n
UPDATE repos
SET name        = :name,
    description = :description,
    url         = :url
WHERE git_id = :git_id;

/*
  Projects
*/

-- :name get-project :? :1
SELECT tag_id as id, p.name as name, p.description, p.image_url
from projects p
where tag_id = :project_id;


-- :name create-project! :insert :raw
-- :command :execute
-- :result: :affected
INSERT INTO projects
    (tag_id, name, description)
VALUES (:tag_id, :name, :description);

-- :name update-project! :! :1
UPDATE projects
SET name        = :name,
    description = :description,
    image_url   = :image
WHERE tag_id = :id;

-- :name delete-project! :! :1
DELETE
FROM projects
WHERE tag_id = :id;


-- :name get-project-entities-of-type :? :*
( -- The entities directly linked to a project
    select i.*, t.*
    from :i:table i
        inner join tags t on i.tag_id = t.id
        inner join tag_relations tr on tr.child_id = i.tag_id
    where tr.parent_id = :project_id
)
UNION
( -- The entities linked to a project via a repo
    select i.*, t2.*
    from :i:table i
        inner join tags t2 on i.tag_id = t2.id
        inner join repos r on i.repo_id = r.tag_id
        inner join tag_relations tr on tr.child_id = r.tag_id
    where tr.parent_id = :project_id
);


-- :name get-project-entities-of-type-count :? :1
select count(*) count
from (
         ( -- The entities directly linked to a project
             select t.id
             from :i:table i
             inner join tags t on i.tag_id = t.id
             inner join tag_relations tr on tr.child_id = i.tag_id
             where tr.parent_id = :project_id
         )
         UNION
         ( -- The entities linked to a project via a repo
             select t2.id
             from :i:table i
             inner join tags t2 on i.tag_id = t2.id
             inner join repos r on i.repo_id = r.tag_id
             inner join tag_relations tr on tr.child_id = r.tag_id
             where tr.parent_id = :project_id
         )) project_issues;

/* ---- LABELS ---- */

-- :name create-label! :insert :raw
INSERT INTO labels
    (git_id, name, description, url, color, repo_id)
VALUES (:git_id, :name, :description, :url, :color, :repo_id);

-- :name get-labels :? :*
SELECT *
FROM labels;

-- :name update-label! :! :n
UPDATE labels
SET name        = :name,
    description = :description,
    url         = :url,
    color       = :color
WHERE git_id = :git_id;

-- :name get-project-labels :? :*
SELECT *
from labels
         INNER JOIN repos using (repo_id)
         INNER JOIN projects using (project_id)
WHERE project_id = :project_id;


/* ---- ISSUES ----- */

-- :name create-issue! :insert :raw
INSERT INTO issues
    (tag_id, git_id, url, title, time, status, repo_id, author)
VALUES (:tag_id, :git_id, :url, :title, :time, :status, :repo_id, :author);

-- :name get-issues :? :*
SELECT *
FROM issues;

-- :name get-project-indirect-issues :? :*
SELECT i.tag_id as id, i.title, i.time as timestamp, i.url, i.repo_id, t.featured, i.status
FROM issues i
         INNER JOIN repos r on i.repo_id = r.tag_id
         INNER JOIN tag_relations tr on tr.child_id = r.tag_id
         INNER JOIN tags t on i.tag_id = t.id
WHERE tr.parent_id = :project_id;

-- :name get-project-indirect-issues-count :? :1
SELECT count(i.tag_id) as count
FROM issues i
         INNER JOIN repos r on i.repo_id = r.tag_id
         INNER JOIN tag_relations tr on tr.child_id = r.tag_id
WHERE tr.parent_id = :project_id;

-- :name get-issue :? :1
SELECT *
FROM issues
WHERE issue_id = :issue_id;

-- :name update-issue! :! :n
UPDATE issues
SET url    = :url,
    title  = :title,
    time   = :time,
    author = :author
WHERE git_id = :git_id;

/* ---- BRANCHES ---- */

-- :name create-branch! :insert :raw
INSERT INTO branches
    (tag_id, commit_sha, name, repo_id)
VALUES (:tag_id, :commit_sha, :name, :repo_id);

-- :name get-branches :? :*
SELECT *
FROM branches;

-- :name get-branch :? :1
SELECT *
FROM branches
WHERE branch_id = :branch_id;

-- :name update-branch! :! :n
UPDATE branches
SET name = :name
WHERE commit_sha = :commit_sha;

-- :name get-project-branches :? :*
SELECT *
from branches
         INNER JOIN repos using (repo_id)
         INNER JOIN projects using (project_id)
WHERE project_id = :project_id;
