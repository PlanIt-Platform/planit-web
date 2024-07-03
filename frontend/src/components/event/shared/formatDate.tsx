export function formatDate(datetime, format: string = 'default'): string {
    if (!datetime) return '';
    const [datePart, timePart] = datetime.includes('T') ? datetime.split('T') : datetime.split(' ');

    switch (format) {
        case 'hourMinute':
            const [hour, minute] = timePart.split(':');
            return `${datePart} ${hour}:${minute}`;
        case 'dateAndTime':
            return `${datePart} ${timePart}`;
        default:
            const dateWithoutSeconds = datetime.slice(0, 16);
            const options: Intl.DateTimeFormatOptions = {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            };
            return new Date(dateWithoutSeconds).toLocaleDateString(undefined, options);
    }
}