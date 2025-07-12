select r.ticket_id, count(r.id)
from ratings r
group by r.ticket_id;

select
	r.id,
	r.ticket_id,
	r.rating,
	rc.name rating_name,
	rc.weight rating_weight,
	r.ticket_id,
	t.subject,
	r.reviewer_id,
	u_reviewer.name reviewer_name,
	r.reviewee_id,
	u_reviewee.name reviewee_name,
	r.created_at
from
	ratings r
	join rating_categories rc on r.rating_category_id  = rc.id
	join tickets t on r.ticket_id = t.id
	join users u_reviewer on r.reviewer_id = u_reviewer.id
	join users u_reviewee on r.reviewee_id = u_reviewee.id
where r.ticket_id  = 270
;