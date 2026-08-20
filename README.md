# poja-starter-template

## Test accounts

Use the following accounts to log into the application:

| Role | Email | Password |
| --- | --- | --- |
| Student | `test@gmail.com` | `test` |
| Teacher | `joe@teacher.com` | `teacher` |
| Admin | `admin@hei.mg` | `Admin123!` |

## Testing the transcript email flow

The test accounts above don't have real inboxes. To actually receive a transcript (PDF uploaded to S3 and sent via AWS SES), an admin can change a student's email from the admin panel to their own personal address. An AWS SES verification request (sandbox mode) is then sent automatically to the new address — just click the confirmation link, then trigger the transcript send (`POST /students/me/transcript/email`) to actually receive it in your inbox.