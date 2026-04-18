package team.incube.flooding.domain.club.service.impl

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

        val allParticipants =
            clubParticipantRepository
                .findAllByClubType(ClubType.MAJOR_CLUB)
                .sortedBy { it.user.studentNumber }

        createClubSheet(workbook, "총합 전공동아리 명단", allParticipants, womanStyle)

        for (grade in 1..3) {
            val gradeData = allParticipants.filter { it.user.grade == grade }
            createClubSheet(workbook, "${grade}학년 전공동아리 명단", gradeData, womanStyle)
        }

        createClubRoomSheet(workbook, headerStyle)

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }

    private fun createClubSheet(
        workbook: XSSFWorkbook,
        sheetName: String,
        participants: List<ClubParticipantJpaEntity>,
        womanStyle: CellStyle,
    ) {
        val sheet = workbook.createSheet(sheetName)
        val clubMap = participants.groupBy { it.club.name }
        val clubNames = clubMap.keys.toList()

        if (clubNames.isEmpty()) return

        val headerRow = sheet.createRow(0)
        clubNames.forEachIndexed { i, name -> headerRow.createCell(i).setCellValue(name) }

        val maxRows = clubMap.values.map { it.size }.maxOrNull() ?: 0
        for (i in 0 until maxRows) {
            val row = sheet.createRow(i + 1)
            clubNames.forEachIndexed { colIndex, clubName ->
                val members = clubMap[clubName] ?: emptyList()
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
        clubNames.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun createClubRoomSheet(
        workbook: XSSFWorkbook,
        headerStyle: CellStyle,
    ) {
        val sheet = workbook.createSheet("활동실 안내")
        val majorClubs = clubRepository.findAllByType(ClubType.MAJOR_CLUB)

        val headerRow = sheet.createRow(0)
        listOf("전공 동아리", "활동실", "담당 선생님").forEachIndexed { i, text ->
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
        (0..2).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createWomanStyle(workbook: XSSFWorkbook) =
        workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

    private fun createHeaderStyle(workbook: XSSFWorkbook) =
        workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
        }
}
