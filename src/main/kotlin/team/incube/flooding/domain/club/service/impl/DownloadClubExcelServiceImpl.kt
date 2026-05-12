package team.incube.flooding.domain.club.service.impl

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.entity.ClubJpaEntity
import team.incube.flooding.domain.club.entity.ClubParticipantJpaEntity
import team.incube.flooding.domain.club.entity.ClubType
import team.incube.flooding.domain.club.repository.ClubParticipantJpaRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.DownloadClubExcelService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.domain.user.entity.Sex
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.io.ByteArrayOutputStream

@Service
class DownloadClubExcelServiceImpl(
    private val clubParticipantRepository: ClubParticipantJpaRepository,
    private val clubRepository: ClubRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DownloadClubExcelService {
    @Transactional(readOnly = true)
    override fun execute(): ByteArray {
        val currentUser = currentUserProvider.getCurrentUser()
        if (currentUser.role != Role.ADMIN) {
            throw ExpectedException("엑셀 다운로드 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        val workbook = XSSFWorkbook()
        val womanStyle = createWomanStyle(workbook)
        val headerStyle = createHeaderStyle(workbook)

        val majorClubs =
            clubRepository
                .findAllByType(ClubType.MAJOR_CLUB)
                .sortedBy { it.name }

        val allParticipants = clubParticipantRepository.findAllByClubType(ClubType.MAJOR_CLUB)

        createClubSheet(workbook, "총합 전공동아리 명단", majorClubs, allParticipants, womanStyle, headerStyle)

        for (grade in 1..3) {
            val gradeParticipants = allParticipants.filter { it.user.grade == grade }
            createClubSheet(workbook, "${grade}학년 전공동아리 명단", majorClubs, gradeParticipants, womanStyle, headerStyle)
        }

        createClubRoomSheet(workbook, majorClubs, headerStyle)

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }

    private fun createClubSheet(
        workbook: XSSFWorkbook,
        sheetName: String,
        majorClubs: List<ClubJpaEntity>,
        participants: List<ClubParticipantJpaEntity>,
        womanStyle: CellStyle,
        headerStyle: CellStyle,
    ) {
        val sheet = workbook.createSheet(sheetName)

        val headerRow = sheet.createRow(0)
        majorClubs.forEachIndexed { i, club ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(club.name)
            cell.setCellStyle(headerStyle)
        }

        if (majorClubs.isEmpty()) return

        val clubMap = participants.groupBy { it.club.id }
        val maxRows = majorClubs.map { club -> clubMap[club.id]?.size ?: 0 }.maxOrNull() ?: 0

        for (i in 0 until maxRows) {
            val row = sheet.createRow(i + 1)
            majorClubs.forEachIndexed { colIndex, club ->
                val members =
                    clubMap[club.id]?.sortedWith(
                        compareBy(
                            { it.user.studentNumber / 1000 },
                            { (it.user.studentNumber % 1000) / 100 },
                            { it.user.studentNumber % 100 },
                        ),
                    ) ?: emptyList()

                if (i < members.size) {
                    val user = members[i].user
                    val cell = row.createCell(colIndex)
                    cell.setCellValue("${user.studentNumber} ${user.name}")

                    if (user.sex == Sex.WOMAN) {
                        cell.setCellStyle(womanStyle)
                    }
                }
            }
        }

        majorClubs.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun createClubRoomSheet(
        workbook: XSSFWorkbook,
        majorClubs: List<ClubJpaEntity>,
        headerStyle: CellStyle,
    ) {
        val sheet = workbook.createSheet("활동실 안내")
        val headerRow = sheet.createRow(0)

        val headers = listOf("전공 동아리", "활동실", "담당 선생님")
        headers.forEachIndexed { i, text ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(text)
            cell.setCellStyle(headerStyle)
        }

        majorClubs.forEachIndexed { index, club ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(club.name)
            row.createCell(1).setCellValue(club.clubRoom?.name ?: "미지정")
            row.createCell(2).setCellValue(club.clubRoom?.teacherName ?: "미지정")
        }

        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun createWomanStyle(workbook: XSSFWorkbook): CellStyle =
        workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

    private fun createHeaderStyle(workbook: XSSFWorkbook): CellStyle =
        workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN

            val font =
                workbook.createFont().apply {
                    bold = true
                }
            setFont(font)
        }
}
