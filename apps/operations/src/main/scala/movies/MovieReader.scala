package es.eriktorr.lambda4s
package movies

trait MovieReader {
  def cumulativeRevenue(): Unit
}

/*
Example: enum



SELECT rating, count(*) AS count
FROM film
GROUP BY rating;
 */

/*
Example: date range

calculate the cumulative revenue of all stores

SELECT payment_date, amount, sum(amount) OVER (ORDER BY payment_date) AS cumulative_revenue
FROM (
  SELECT CAST(payment_date AS DATE) AS payment_date, SUM(amount) AS amount
  FROM payment
  WHERE payment_date BETWEEN '2005-06-01' AND '2005-12-31'
  GROUP BY CAST(payment_date AS DATE)
) p
ORDER BY payment_date;
 */
